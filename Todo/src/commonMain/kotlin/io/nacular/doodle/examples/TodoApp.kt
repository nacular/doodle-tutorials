package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.list.listEditor
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.height
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.event.PointerListener.Companion.released
import io.nacular.doodle.examples.DataStore.DataStoreListModel
import io.nacular.doodle.examples.DataStore.Filter
import io.nacular.doodle.examples.DataStore.Filter.Active
import io.nacular.doodle.examples.DataStore.Filter.Completed
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.layout.constraints.plus
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.BasicListBehavior
import io.nacular.doodle.theme.basic.list.BasicVerticalListPositioner
import io.nacular.doodle.theme.basic.list.TextEditOperation
import io.nacular.doodle.theme.basic.list.basicItemGenerator
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Insert
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.Result.Companion.success

/**
 * This app is designed to run both top-level and nested. The filter buttons use hyperlinks in the spec,
 * but we will delegate their definition to the app creator to allow a different approach when nested
 * (i.e. using buttons instead of hyperlinks).
 *
 * The app will then use this provider to create the filter buttons.
 */
interface FilterButtonProvider {
    operator fun invoke(text: String, filter: Filter? = null, behavior: Behavior<Button>): Button
}

/**
 * Default implementation intended for use when app is top-level. It handles routing and provides
 * hyperlinks for the filter buttons.
 */
class LinkFilterButtonProvider(private val dataStore: DataStore, router: Router, private val linkStyler: NativeHyperLinkStyler): FilterButtonProvider {
    init {
        router["/"         ] = { dataStore.filter = null      }
        router["/active"   ] = { dataStore.filter = Active    }
        router["/completed"] = { dataStore.filter = Completed }
        router.fireAction()
    }

    override fun invoke(text: String, filter: Filter?, behavior: Behavior<Button>): Button {
        val url = when (filter) {
            Active    -> "#/active"
            Completed -> "#/completed"
            else      -> "#/"
        }

        return HyperLink(url = url, text = text).apply {
            this.behavior            = linkStyler(this, behavior) as Behavior<Button>
            this.acceptsThemes       = false
            dataStore.filterChanged += { rerender() }
        }
    }
}

/**
 * General styling config
 */
data class TodoConfig(
    val listFont              : Font,
    val titleFont             : Font,
    val lineColor             : Color  = Color(0xEDEDEDu),
    val filterFont            : Font,
    val footerFont            : Font,
    val headerColor           : Color  = Color(0xAF2F2Fu) opacity 0.15f,
    val deleteColor           : Color  = Color(0xCC9A9Au),
    val appBackground         : Color  = Color(0xF5F5F5u),
    val boldFooterFont        : Font,
    val selectAllColor        : Color  = Color(0x737373u),
    val checkForeground       : Image,
    val checkBackground       : Image,
    val placeHolderFont       : Font,
    val placeHolderText       : String = "What needs to be done?",
    val placeHolderColor      : Color  = Color(0xE6E6E6u),
    val labelForeground       : Color  = Color(0x4D4D4Du),
    val footerForeground      : Color  = Color(0xBFBFBFu),
    val deleteHoverColor      : Color  = Color(0xAF5B5Eu),
    val taskCompletedColor    : Color  = Color(0xD9D9D9u),
    val clearCompletedText    : String = "Clear completed",
    val textFieldBackground   : Color  = White,
    val filterButtonForeground: Color  = Color(0x777777u),
)

/**
 * A [TextEditOperation] that translates a String to a [Task]. It also customizes the textField to fit the app's styling
 */
private class TaskEditOperation(focusManager: FocusManager?, list: MutableList<Task, *>, task: Task, current: View): TextEditOperation<Task>(focusManager, TaskEncoder(task.completed), list, task, current) {
    private class TaskEncoder(private val completed: Boolean = false): Encoder<Task, String> {
        override fun decode(b: String) = success(Task(b, completed))
        override fun encode(a: Task  ) = success(a.text)
    }

    init {
        textField.fitText         = emptySet()
        textField.backgroundColor = Transparent
    }

    override fun invoke() = container {
        children += textField
        layout    = constrain(textField) { it.edges eq parent.edges + Insets(top = 1.0, left = 58.0, bottom = 1.0) }
    }
}

/**
 * This is the main view of the app. It contains all the visual elements.
 *
 * @property config that provides styling and resources (i.e. font, images)
 * @property dataStore that tracks the overall state of tasks
 * @property linkStyler used to wrap our custom Hyperlink Behavior in a native one so we get the default link behavior as well
 * @property textMetrics used to measure text
 * @property focusManager used to control focus in the app
 * @property filterButtonProvider used to create the filter buttons
 */
private class TodoView(
    private val config              : TodoConfig,
    private val dataStore           : DataStore,
    private val linkStyler          : NativeHyperLinkStyler,
    private val textMetrics         : TextMetrics,
    private val focusManager        : FocusManager,
    private val filterButtonProvider: FilterButtonProvider
): View() {

    init {
        val header = Label("todos").apply {
            font            = config.titleFont
            behavior        = CommonLabelBehavior(textMetrics)
            acceptsThemes   = false
            foregroundColor = config.headerColor
        }
        lateinit var list: View
        val footer   = Footer(textMetrics, linkStyler, config)
        val taskList = object: Container() {
            init {
                clipCanvasToBounds = false

                // Maps tasks to TaskRow and updates them when recycled
                val visualizer = itemVisualizer<Task, IndexedItem> { item, previous, _ ->
                    when (previous) {
                        is TaskRow -> previous.also { it.task = item }
                        else       -> TaskRow(config, dataStore, item)
                    }
                }

                // List containing Tasks. It is mutable since items can be edited
                list = MutableList(DataStoreListModel(dataStore), itemVisualizer = visualizer).apply {
                    val rowHeight = 58.0
                    font          = config.listFont
                    cellAlignment = fill
                    editor        = listEditor { list, row, _, current -> TaskEditOperation(focusManager, list, row, current) }
                    behavior      = BasicListBehavior(focusManager,
                        basicItemGenerator {
                            // edit when double-clicked
                            pointerChanged += released { event ->
                                if (event.clickCount >= 2) { this@apply.startEditing(index).also { event.consume() } }
                            }
                        },
                        BasicVerticalListPositioner(rowHeight),
                        PatternPaint(Size(10.0, rowHeight)) {
                            rect(Rectangle(size = this.size), color = White)
                            line(Point(y = 1), Point(10, 1), Stroke(config.lineColor))
                        },
                    )

                    itemsChanged += { _, differences ->
                        // Scroll when a single item added
                        var numAdded    = 0
                        var numRemoved  = 0
                        var indexInList = 0

                        differences.forEach {
                            when (it) {
                                is Insert -> {
                                    if (it.items.size > 1) {
                                        return@forEach
                                    }

                                    ++numAdded
                                    ++indexInList
                                }
                                is Delete -> {
                                    numRemoved += it.items.size
                                }
                                else      -> indexInList += it.items.size
                            }
                        }

                        if (numAdded == 1 && numRemoved <= 1) {
                            scrollTo(indexInList)
                        }
                    }

                    boundsChanged += { _, old, new ->
                        if (old.width != new.width || old.height != new.height) {
                            this@TodoView.relayout()
                        }
                    }
                }

                children += listOf(
                    TaskCreationBox(focusManager, textMetrics, config, dataStore),
                    ScrollPanel    (list).apply { contentWidthConstraints = { it eq width - verticalScrollBarWidth } },
                    FilterBox      (config, dataStore, textMetrics, filterButtonProvider)
                )

                layout = constrain(children[0], children[1], children[2]) { input, panel, filter ->
                    listOf(input, panel, filter).forEach { it.width eq parent.width }

                    input.top     eq 0
                    input.height.preserve

                    panel.top     eq input.bottom
                    panel.height  eq panel.idealHeight strength Strong + 3

                    if (children[2].visible) {
                        filter.top    eq panel.bottom
                        filter.height.preserve
                        parent.height eq filter.bottom
                    } else {
                        parent.height eq input.bottom
                    }
                }
            }

            override fun render(canvas: Canvas) {
                canvas.outerShadow(vertical =  2.0, blurRadius =  4.0, color = Black opacity 0.2f) {
                    outerShadow   (vertical = 25.0, blurRadius = 50.0, color = Black opacity 0.1f) {
                        // Create stacked effect
                        if (!dataStore.isEmpty) {
                            rect(bounds.atOrigin.inset(Insets(top = height, left = 8.0, right = 8.0, bottom = -8.0)), color = White)
                            rect(bounds.atOrigin.inset(Insets(top = height, left = 4.0, right = 4.0, bottom = -4.0)), color = White)
                        }
                        rect(bounds.atOrigin, color = White)
                    }
                }
            }
        }

        children += listOf(header, taskList, footer)

        list.boundsChanged += { _,_,_ -> doLayout() }

        layout = constrain(header, taskList, footer) { header, body, footer ->
            listOf(header, body, footer).forEach { it.centerX eq parent.centerX }

            header.top eq 9
            header.height.preserve

            body.top      eq     header.bottom + 5
            body.width    lessEq parent.width - 10
            body.width    eq     550    strength Strong
            body.height   eq     body.idealHeight

            footer.top    eq     body.bottom.readOnly + 65
            footer.width  eq     body.width
            footer.height eq     footer.idealHeight
        }
    }
}

/**
 * Todo App based on TodoMVC
 */
class TodoApp(display             : Display,
              uiDispatcher        : CoroutineDispatcher,
              fonts               : FontLoader,
              theme               : DynamicTheme,
              themes              : ThemeManager,
  private val images              : ImageLoader,
              dataStore           : DataStore,
              linkStyler          : NativeHyperLinkStyler,
              textMetrics         : TextMetrics,
              focusManager        : FocusManager,
              filterButtonProvider: FilterButtonProvider): Application {
//sampleStart
    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // Launch coroutine to fetch fonts/images
        appScope.launch(uiDispatcher) {
            val titleFont  = fonts            { size = 100; weight = 100; families = listOf("Helvetica Neue", "Helvetica", "Arial", "sans-serif") }!!
            val listFont   = fonts(titleFont) { size =  24 }!!
            val footerFont = fonts(titleFont) { size =  10 }!!
            val config     = TodoConfig(
                listFont        = listFont,
                titleFont       = titleFont,
                footerFont      = footerFont,
                filterFont      = fonts(titleFont ) { size   = 14     }!!,
                boldFooterFont  = fonts(footerFont) { weight = 400    }!!,
                placeHolderFont = fonts(listFont  ) { style  = Italic }!!,
                checkForeground = checkForegroundImage(),
                checkBackground = checkBackgroundImage(),
            )

            // install theme
            themes.selected = theme

            display += TodoView(config, dataStore, linkStyler, textMetrics, focusManager, filterButtonProvider)

            display.layout = constrain(display.children[0]) { it.edges eq parent.edges }

            display.fill(config.appBackground.paint)
        }
    }
//sampleEnd

    override fun shutdown() {}

    private suspend fun checkForegroundImage() = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E")!!
    private suspend fun checkBackgroundImage() = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E")!!
}