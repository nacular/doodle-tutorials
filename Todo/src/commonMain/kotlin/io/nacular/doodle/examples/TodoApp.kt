@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.*
import io.nacular.doodle.controls.buttons.*
import io.nacular.doodle.controls.list.*
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.text.*
import io.nacular.doodle.controls.theme.*
import io.nacular.doodle.core.*
import io.nacular.doodle.drawing.*
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.event.*
import io.nacular.doodle.event.KeyCode.Companion.Enter
import io.nacular.doodle.event.PointerListener.Companion.released
import io.nacular.doodle.examples.DataStore.DataStoreListModel
import io.nacular.doodle.examples.DataStore.Filter
import io.nacular.doodle.examples.DataStore.Filter.*
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.*
import io.nacular.doodle.image.*
import io.nacular.doodle.layout.*
import io.nacular.doodle.layout.WidthSource.Parent
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.system.Cursor.Companion.Text
import io.nacular.doodle.text.TextDecoration.Companion.LineThrough
import io.nacular.doodle.text.TextDecoration.Companion.UnderLine
import io.nacular.doodle.text.TextDecoration.Line.*
import io.nacular.doodle.text.invoke
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.*
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment.*
import io.nacular.doodle.utils.VerticalAlignment.Bottom
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates.observable
import io.nacular.doodle.event.KeyListener.Companion.released as keyReleased

interface FilterButtonProvider {
    operator fun invoke(text: String, filter: Filter? = null, behavior: Behavior<Button>): Button
}

private data class TodoConfig(
        val listFont       : Font,
        val titleFont      : Font,
        val filterFont     : Font,
        val footerFont     : Font,
        val boldFooterFont : Font,
        val checkForeground: Image,
        val checkBackground: Image,
        val placeHolderFont: Font
)

private class TaskEditOperation(focusManager: FocusManager?, list: MutableList<Task, *>, task: Task, current: View): TextEditOperation<Task>(focusManager, TaskEncoder(task.completed), list, task, current) {
    private class TaskEncoder(private val completed: Boolean = false): Encoder<Task, String> {
        override fun decode(b: String) = Task(b, completed)
        override fun encode(a: Task  ) = a.text
    }

    init {
        textField.fitText         = emptySet()
        textField.backgroundColor = Transparent
    }

    override fun invoke() = container {
        children += textField
        layout    = constrain(textField) { fill(Insets(top = 1.0, left = 58.0, bottom = 1.0))(it) }
    }
}

internal class LinkFilterButtonProvider(private val dataStore: DataStore, router: Router, private val linkStyler: NativeLinkStyler): FilterButtonProvider {
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

private class Todo(private val config              : TodoConfig,
                   private val dataStore           : DataStore,
                   private val linkStyler          : NativeLinkStyler,
                   private val textMetrics         : TextMetrics,
                   private val focusManager        : FocusManager,
                   private val filterButtonProvider: FilterButtonProvider): View() {
    private inner class TaskRow(task: Task): View() {
        var task: Task by observable(task) { _,_,new ->
            check.selected = new.completed
            when {
                new.completed -> label.styledText = (Color(0xD9D9D9u)) { LineThrough(new.text) }
                else          -> label.text       = new.text
            }
        }

        private val check = CheckBox().apply {
            selectedChanged += { _,_,_ -> dataStore.mark(this@TaskRow.task, completed = selected) }

            val imageSubRect = Rectangle(6, 0, 34, 40)

            behavior = simpleButtonRenderer { button, canvas ->
                val destination = button.bounds.atOrigin.inset(Insets(left = 13.0, right = 13.0, top = 9.0, bottom = 9.0))

                if (button.selected) canvas.image(config.checkForeground, source = imageSubRect, destination = destination)

                canvas.image(config.checkBackground, source = imageSubRect, destination = destination)
            }
        }

        private val label = Label(task.text, horizontalAlignment = Left).apply { fitText = emptySet(); foregroundColor = Color(0x4D4D4Du) }

        private val delete = PushButton().apply {
            fired    += { dataStore.remove(this@TaskRow.task) }
            visible   = false
            behavior  = simpleButtonRenderer { button, canvas ->
                val iconBounds = bounds.atOrigin.inset(Insets(top = 22.0, bottom = 22.0, left = 23.0, right = 23.0))

                val stroke = Stroke(when {
                    button.model.pointerOver -> Color(0xAF5B5Eu) // TODO: Animate instead
                    else                     -> Color(0xCC9A9Au)
                })

                canvas.line(iconBounds.position, Point(iconBounds.right, iconBounds.bottom), stroke)
                canvas.line(Point(iconBounds.right, iconBounds.y), Point(iconBounds.x, iconBounds.bottom), stroke)
            }
        }

        init {
            this.task = task
            children += listOf   (check, label, delete)
            layout    = constrain(check, label, delete) { check, label, delete ->
                listOf(check, label, delete).forEach { it.height = parent.height  }
                listOf(check,        delete).forEach { it.width  = constant(60.0) }
                label.left   = check.right
                label.right  = delete.left
                delete.right = parent.right
            }

            pointerChanged += object: PointerListener {
                override fun entered(event: PointerEvent) { delete.visible = true  }
                override fun exited (event: PointerEvent) { delete.visible = false }
            }
        }
    }

    private inner class CreationBox: View() {
        init {
            cursor    = Text
            height    = 65.0
            children += PushButton("❯").apply {
                font     = config.listFont
                width    = 60.0
                fired   += { dataStore.markAll(completed = dataStore.active.isNotEmpty()) }
                cursor   = Default
                visible  = !dataStore.isEmpty
                behavior = simpleTextButtonRenderer(textMetrics) { button, canvas ->
                    canvas.rotate(around = Point(button.width/2, button.height/2), by = 90 * degrees) {
                        text(button.text, font(button), textPosition(button), if (dataStore.active.isEmpty()) Color(0x737373u) else Color(0xE6E6E6u))
                    }
                }

                dataStore.changed += {
                    visible = !it.isEmpty
                    rerender()
                }
            }

            children += TextField().apply {
                font              = config.listFont
                placeHolder       = "What needs to be done?"
                borderVisible     = false
                placeHolderFont   = config.placeHolderFont
                foregroundColor   = Color(0x4D4D4Du)
                placeHolderColor  = Color(0xE6E6E6u)
                keyChanged       += keyReleased { event ->
                    if (event.code == Enter && text.isNotBlank()) {
                        dataStore.add(Task(text.trim()))
                        text = ""
                    }
                }
            }

            layout = constrain(children[0], children[1]) { button, textField ->
                listOf(button, textField).forEach { it.height = parent.height }
                textField.left  = button.right
                textField.right = parent.right
            }

            pointerChanged += released { focusManager.requestFocus(children[1]) }
        }

        override fun addedToDisplay() { focusManager.requestFocus(children[1]) }
    }

    private inner class FilterBox: View() {
        private fun filterButtonBehavior(widthInset: Double = 16.0, renderBlock: CommonTextButtonBehavior<HyperLink>.(HyperLink, Canvas) -> Unit): Behavior<Button> = object: CommonTextButtonBehavior<HyperLink>(textMetrics) {
            override fun install(view: HyperLink) {
                super.install(view)
                view.size = textMetrics.size(view.text, config.filterFont).run { Size(width + widthInset, height + 8.0) }
            }

            override fun render(view: HyperLink, canvas: Canvas) {
                renderBlock(this, view, canvas)
                canvas.text(view.text, font(view), textPosition(view), ColorFill(Color(0x777777u)))
            }
        } as Behavior<Button>

        private fun filterButton(text: String, filter: Filter? = null) = filterButtonProvider(text, filter, filterButtonBehavior { hyperLink, canvas ->
            val selected = hyperLink.model.selected || dataStore.filter == filter
            if (hyperLink.model.pointerOver || selected) {
                canvas.rect(hyperLink.bounds.atOrigin.inset(0.5), radius = 3.0, stroke = Stroke(when {
                    selected -> Color(0xAF2F2Fu) opacity 0.2f
                    else     -> Color(0xAF2F2Fu) opacity 0.1f
                }))
            }
        }).apply {
            font            = config.filterFont
            foregroundColor = Color(0x777777u)
        }

        private val itemsLeft = Label().apply { foregroundColor = Color(0x4D4D4Du) }
        private val clearAll  = PushButton("Clear completed").apply {
            behavior = filterButtonBehavior(widthInset = 30.0) { view, canvas ->
                val color = Color(0x777777u)
                when {
                    model.pointerOver -> canvas.text(UnderLine { color { font(view)(view.text) } }, textPosition(view))
                    else              -> canvas.text(view.text, font(view), textPosition(view), ColorFill(color))
                }
            }

            fired += { dataStore.removeCompleted() }
        }

        private fun update() {
            clearAll.visible = dataStore.completed.isNotEmpty()
            visible    = !dataStore.isEmpty
            itemsLeft.text = dataStore.active.size.let { active -> "$active ${if (active > 1) "Items" else "Item"} left" }
        }

        init {
            update()
            val all       = filterButton("All"                 )
            val active    = filterButton("Active",    Active   )
            val completed = filterButton("Completed", Completed)
            dataStore.changed += { update() }
            font      = config.filterFont
            height    = 41.0
            children += listOf(itemsLeft, all, active, completed, clearAll)
            layout    = constrain(itemsLeft, all, active, completed, clearAll) { label, all_, active_, completed_, clearAll ->
                listOf(label, all_, active_, completed_, clearAll).forEach { it.centerY = parent.centerY }
                val spacing     = 6.0
                label.left      = parent.left  + 15
                clearAll.right  = parent.right
                clearAll.height = parent.height
                all_.left        = parent.left + (parent.width - { all.width + active.width + completed.width + spacing * 2 }) / 2
                active_.left     = all_.right    + spacing
                completed_.left  = active_.right + spacing
            }
        }

        override fun render(canvas: Canvas) {
            canvas.line(Point(y = 0.5), Point(width, 0.5), Stroke(Color(0xEDEDEDu)))
        }
    }

    private inner class Footer: View() {
        private fun footerLabel(text: String) = Label(text).apply {
            font            = config.footerFont
            fitText         = setOf(TextFit.Height)
            acceptsThemes   = false
            foregroundColor = Color(0xBFBFBFu)
            behavior        = object: CommonLabelBehavior(textMetrics) {
                override fun render(view: Label, canvas: Canvas) {
                    canvas.outerShadow(vertical = 1.0, blurRadius = 0.0, color = White opacity 0.5f) {
                        super.render(view, this)
                    }
                }
            }
        }

        private fun linkLabel(text: String, linkText: String, url: String) = container {
            val link = HyperLink(url, linkText).apply {
                font            = config.boldFooterFont
                acceptsThemes   = false
                foregroundColor = Color(0xBFBFBFu)
                behavior        = linkStyler(this, object: CommonTextButtonBehavior<HyperLink>(textMetrics) {
                    override fun install(view: HyperLink) {
                        super.install(view)
                        size = textMetrics.size(view.text, view.font)
                    }

                    override fun render(view: HyperLink, canvas: Canvas) {
                        val styledText = view.foregroundColor { view.font(view.text) }.let {
                            if (view.model.pointerOver) UnderLine{ it } else it
                        }

                        canvas.outerShadow(vertical = 1.0, blurRadius = 0.0, color = White opacity 0.5f) {
                            text(styledText, at = textPosition(view))
                        }
                    }
                }) as Behavior<Button>
            }

            children += listOf(footerLabel(text), link)
            layout    = HorizontalFlowLayout(justification = Center, verticalAlignment = Bottom, horizontalSpacing = 0.0) then {
                height = children.maxOf { it.height }
            }
        }

        init {
            children += footerLabel("Double-click to edit a todo")
            children += linkLabel  ("Created with ", "Doodle", "https://github.com/nacular/doodle")
            children += linkLabel  ("Part of ", "TodoMVC", "http://todomvc.com")

            layout = ListLayout(widthSource = Parent, spacing = 10) then {
                height = children.last().bounds.bottom
            }
        }
    }

    init {
        val header = Label("todos").apply {
            font            = config.titleFont
            behavior        = CommonLabelBehavior(textMetrics)
            acceptsThemes   = false
            foregroundColor = Color(0xAF2F2FU) opacity 0.15f
        }
        val footer = Footer()
        val body   = object: Container() {
            init {
                clipCanvasToBounds = false

                val visualizer = itemVisualizer<Task, IndexedIem> { item, previous, _ ->
                    when (previous) {
                        is TaskRow -> previous.also { it.task = item }
                        else       -> TaskRow(item)
                    }
                }

                val list = MutableList(DataStoreListModel(dataStore), itemVisualizer = visualizer).apply {
                    val rowHeight = 58.0
                    font          = config.listFont
                    cellAlignment = fill
                    editor        = listEditor { list, row, _, current -> TaskEditOperation(focusManager, list, row, current) }
                    behavior      = BasicListBehavior(focusManager,
                        basicItemGenerator {
                            pointerChanged += released { event ->
                                if (event.clickCount >= 2) {
                                    this@apply.startEditing(index)
                                    event.consume()
                                }
                            }
                        },
                        PatternFill(Size(10.0, rowHeight)) {
                            line(Point(y = 1), Point(10, 1), Stroke(Color(0xEDEDEDu)))
                        },
                        rowHeight)

                    itemsChanged += { _, removed, added, moved ->
                        if (added.size == 1 && moved.isEmpty() && removed.size <= 1) {
                            scrollTo(added.keys.first())
                        }
                    }

                    boundsChanged += { _, old, new ->
                        if (old.width != new.width || old.height != new.height) {
                            this@Todo.relayout()
                        }
                    }
                }

                children += listOf(CreationBox(), ScrollPanel(list).apply { contentWidthConstraints = { parent.width } }, FilterBox())

                layout = constrain(children[0], children[1], children[2]) { input, panel, filter ->
                    listOf(input, panel, filter).forEach { it.width = parent.width }
                    input.top     = parent.top
                    panel.top     = input.bottom
                    panel.height  = parent.height - input.height - filter.height
                    filter.bottom = parent.bottom
                } then {
                    val oldHeight = height

                    val minHeight = children[0].height + (children[2].takeIf { it.visible }?.height ?: 0.0)

                    height = max(min(children[0].height + list.height + (children[2].takeIf { it.visible }?.height ?: 0.0),
                            parent!!.height - (y + footer.height + 65 + 5)), minHeight)

                    if (oldHeight != height) doLayout()
                }
            }

            override fun render(canvas: Canvas) {
                canvas.outerShadow(vertical =  2.0, blurRadius =  4.0, color = Black opacity 0.2f) {
                    outerShadow   (vertical = 25.0, blurRadius = 50.0, color = Black opacity 0.1f) {
                        if (!dataStore.isEmpty) {
                            rect(bounds.atOrigin.inset(Insets(top = height, left = 8.0, right = 8.0, bottom = -8.0)), color = White)
                            rect(bounds.atOrigin.inset(Insets(top = height, left = 4.0, right = 4.0, bottom = -4.0)), color = White)
                        }
                        rect(bounds.atOrigin, color = White)
                    }
                }
            }
        }

        children += listOf(header, body, footer)

        layout = constrain(header, body, footer) { header, body, footer ->
            listOf(header, body, footer).forEach { it.centerX = parent.centerX }
            header.top   = parent.top      +  9
            body.top     = header.bottom   +  5
            body.width   = min(550.0, parent.width)
            footer.top   = body.bottom + 65
            footer.width = body.width
        } then {
            body.relayout()
        }
    }
}

class TodoApp(display             : Display,
              fonts               : FontLoader,
              theme               : DynamicTheme,
              themes              : ThemeManager,
              images              : ImageLoader,
              dataStore           : DataStore,
              linkStyler          : NativeLinkStyler,
              textMetrics         : TextMetrics,
              focusManager        : FocusManager,
              filterButtonProvider: FilterButtonProvider): Application {
    init {
        GlobalScope.launch {
            val titleFont  = fonts            { family = "Helvetica Neue"; size = 100; weight = 100 }
            val listFont   = fonts(titleFont) {                            size =  24               }
            val footerFont = fonts(titleFont) {                            size =  10               }

            val config = TodoConfig(
                    listFont        = listFont,
                    titleFont       = titleFont,
                    filterFont      = fonts(titleFont ) { size   = 14     },
                    footerFont      = footerFont,
                    boldFooterFont  = fonts(footerFont) { weight = 400    },
                    checkForeground = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E")!!,
                    checkBackground = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E")!!,
                    placeHolderFont = fonts(listFont  ) { style  = Italic }
            )

            themes.selected = theme

            display += Todo(config, dataStore, linkStyler, textMetrics, focusManager, filterButtonProvider)

            display.layout = constrain(display.children[0]) { fill(it) }
            display.fill(ColorFill(Color(0xF5F5F5u)))
        }
    }

    override fun shutdown() {}
}