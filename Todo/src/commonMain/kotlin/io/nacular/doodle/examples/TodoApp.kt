@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.*
import io.nacular.doodle.controls.buttons.*
import io.nacular.doodle.controls.list.*
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.text.*
import io.nacular.doodle.controls.theme.*
import io.nacular.doodle.core.*
import io.nacular.doodle.drawing.*
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.event.*
import io.nacular.doodle.event.KeyListener.Companion.released as keyReleased
import io.nacular.doodle.event.PointerListener.Companion.released
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.*
import io.nacular.doodle.image.*
import io.nacular.doodle.layout.*
import io.nacular.doodle.layout.WidthSource.Parent
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.text.TextDecoration
import io.nacular.doodle.text.TextDecoration.Line.*
import io.nacular.doodle.text.invoke
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.*
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreationBox(private val focusManager: FocusManager, font: Font, placeHolderFont: Font, dataStore: DataStore): View() {
    init {
        val color = Color(0x4D4D4Du)

        val button = PushButton().apply {
            cursor = Default
            width  = 60.0
            fired += { when {
                dataStore.active.isNotEmpty() -> dataStore.markAllCompleted()
                else                          -> dataStore.markAllActive   ()
            } }
            visible   = !dataStore.isEmpty
            focusable = false
            behavior  = simpleButtonRenderer { button, canvas ->
                val thickness = 4.0
                val polyBounds = button.bounds.atOrigin.inset(Insets(top = 27.0, bottom = 27.0, left = 22.0, right = 22.0))

                val poly = ConvexPolygon(
                    Point(polyBounds.center.x, polyBounds.bottom),
                    Point(polyBounds.x, polyBounds.y + thickness),
                    polyBounds.position,
                    Point(polyBounds.center.x, polyBounds.bottom - thickness),
                    Point(polyBounds.right, polyBounds.y),
                    Point(polyBounds.right, polyBounds.y + thickness)
                )

                canvas.poly(poly, color = when {
                    dataStore.active.isEmpty() -> Color(0x737373u)
                    else                       -> Color(0xE6E6E6u)
                })
            }
        }

        height    = 65.0
        children += button
        children += TextField().apply {
            this.font        = font
            borderVisible    = false
            foregroundColor  = color
            placeHolder      = "What needs to be done?"
            placeHolderColor = Color(0xE6E6E6u)
            this.placeHolderFont = placeHolderFont

            keyChanged += keyReleased { event ->
                when {
                    event.code == KeyCode.Enter && text.isNotBlank() -> {
                        dataStore.add(text)

                        text = ""
                    }
                }
            }
        }

        dataStore.tasksChanged += {
            button.visible = !it.isEmpty
            button.rerender()
        }

        layout = constrain(button, children[1]) { button, textField ->
            button.height    = parent.height
            textField.height = button.height
            textField.left   = button.right
            textField.right  = parent.right
        }
        cursor = Cursor.Text

        pointerChanged += released {
            focusManager.requestFocus(children[1])
        }
    }

    override fun addedToDisplay() {
        focusManager.requestFocus(children[1])
    }
}

private val lineThrough = TextDecoration(lines = setOf(Through))
private val underLine   = TextDecoration(lines = setOf(Under  ))

class TaskView(task: Task, dataStore: DataStore, toggleBackground: Image, selectedImage: Image): View() {
    var task = task
        set(new) {
            field          = new
            check.selected = field.completed
            when {
                field.completed -> label.styledText = (Color(0xD9D9D9u)) { lineThrough(new.text) }
                else            -> label.text       = new.text
            }
        }

    private val check = CheckBox().apply {
        selectedChanged += { _,_,selected ->
            when (selected) {
                true -> dataStore.markCompleted(this@TaskView.task)
                else -> dataStore.markActive   (this@TaskView.task)
            }
        }

        val source = Rectangle(6, 0, 34, 40)

        behavior = simpleButtonRenderer { button, canvas ->
            val destination = button.bounds.atOrigin.inset(Insets(left = 13.0, right = 13.0, top = 9.0, bottom = 9.0))

            if (button.selected) canvas.image(selectedImage, source = source, destination = destination)

            canvas.image(toggleBackground, source = source, destination = destination)
        }
    }

    private val label = Label(task.text).apply {
        fitText             = emptySet()
        horizontalAlignment = Left
    }

    private val delete = PushButton().apply {
        visible    = false
        focusable  = false
        fired     += { dataStore.remove(this@TaskView.task) }
        behavior   = simpleButtonRenderer { button, canvas ->
            val iconBounds = bounds.atOrigin.inset(Insets(top = 22.0, bottom = 22.0, left = 23.0, right = 23.0))

            val color = when {
                button.model.pointerOver -> Color(0xAF5B5Eu) // TODO: Animate instead
                else                     -> Color(0xCC9A9Au)
            }

            canvas.line(iconBounds.position, Point(iconBounds.right, iconBounds.bottom), Stroke(color))
            canvas.line(Point(iconBounds.right, iconBounds.y), Point(iconBounds.x, iconBounds.bottom), Stroke(color))
        }
    }

    init {
        children += listOf(check, label, delete)

        layout = constrain(check, label, delete) { check, label, delete ->
            check.width   = constant(60.0)
            check.height  = parent.height
            label.left    = check.right
            label.right   = delete.left
            label.height  = check.height
            delete.width  = check.width
            delete.right  = parent.right
            delete.height = label.height
        }

        pointerChanged += object: PointerListener {
            override fun entered(event: PointerEvent) { delete.visible = true  }
            override fun exited (event: PointerEvent) { delete.visible = false }
        }

        this.task = task
    }
}

data class TodoConfig(
        val largeFont      : Font,
        val mediumFont     : Font,
        val smallFont      : Font,
        val italicFont     : Font,
        val footerFont     : Font,
        val boldFooterFont : Font,
        val checkBackground: Image,
        val checkImage     : Image
)

class TaskEditOperation(focusManager: FocusManager?,
                        list        : MutableList<Task, *>,
                        row         : Task,
                        current     : View): TextEditOperation<Task>(focusManager, TaskEncoder(row.completed), list, row, current) {
    private class TaskEncoder(private val completed: Boolean = false): Encoder<Task, String> {
        override fun decode(b: String) = Task(b, completed)
        override fun encode(a: Task  ) = a.text
    }

    init {
        textField.fitText         = emptySet()
        textField.backgroundColor = Transparent
    }
//    override val cancelOnFocusLost = true
    override fun invoke() = container {
        children += textField
        layout    = constrain(textField) {
            it.top    = parent.top  +  1
            it.left   = parent.left + 58
            it.right  = parent.right
            it.height = parent.height
        }
    }
}

class FooterInfo(private val textMetrics: TextMetrics, private val config: TodoConfig): View() {
    private fun footerLabel(text: String) = Label(text).apply {
        font            = config.footerFont
        fitText         = setOf(TextFit.Height)
        acceptsThemes   = false
        foregroundColor = Color(0xBFBFBFu)
        backgroundColor = null
        behavior        = object: CommonLabelBehavior(textMetrics) {
            override fun render(view: Label, canvas: Canvas) {
                canvas.outerShadow(vertical = 1.0, blurRadius = 0.0, color = White opacity 0.5f) {
                    super.render(view, this)
                }
            }
        }
    }

    private fun linkLabel(text: String, linkText: String, url: String) = container {
        val link  = HyperLink(url, linkText).apply { foregroundColor = Color(0xBFBFBFu); font = config.boldFooterFont }
        children += listOf(footerLabel(text), link)
        layout    = HorizontalFlowLayout(justification = Center) then {
            height = children.maxOf { it.height }
        }
    }

    init {
        children += footerLabel("Double-click to edit a todo")
        children += linkLabel("Created with ", "Doodle", "https://github.com/nacular/doodle")
        children += linkLabel("Part of ", "TodoMVC", "http://todomvc.com")

        layout = ListLayout(widthSource = Parent, spacing = 10) then {
            height = children.lastOrNull()?.bounds?.bottom ?: 0.0
        }
    }
}

open class FooterButtonBehavior(
        private val textMetrics: TextMetrics,
        private val font       : Font?,
        private val widthInset : Double = 16.0,
        private val heightInset: Double =  8.0
): CommonTextButtonBehavior<Button>(textMetrics) {

    override fun install(view: Button) {
        super.install(view)

        view.size = textMetrics.size(view.text, font).run { Size(width + widthInset, height + heightInset) }
    }

    override fun render(view: Button, canvas: Canvas) {
        if (view.text.isNotBlank()) {
            canvas.text(view.text, font(view), textPosition(view), ColorFill(Color(0x777777u)))
        }
    }
}

class FilterBox(private val textMetrics: TextMetrics, private val dataStore: DataStore, font: Font): View() {
    private fun filterButton(text: String): ToggleButton = ToggleButton(text).apply {
        behavior = object: FooterButtonBehavior(textMetrics, this@FilterBox.font) {
            override fun render(view: Button, canvas: Canvas) {
                if (view.model.pointerOver || view.model.selected) {
                    canvas.rect(view.bounds.atOrigin.inset(0.5), radius = 3.0, stroke = Stroke(when {
                        view.model.selected -> Color(0xAF2F2Fu) opacity 0.2f
                        else                -> Color(0xAF2F2Fu) opacity 0.1f
                    }))
                }

                super.render(view, canvas)
            }
        }
    }

    private fun updateLabel(label: Label) {
        label.text = dataStore.active.size.let { active -> "$active ${if (active > 1) "Items" else "Item"} left" }
    }

    init {
        this.font     = font

        val label     = Label()
        val all       = filterButton("All"            )
        val active    = filterButton("Active"         )
        val completed = filterButton("Completed"      )
        val clearAll  = PushButton  ("Clear completed").apply {
            behavior = object: FooterButtonBehavior(textMetrics, font, widthInset = 30.0) {
                override fun render(view: Button, canvas: Canvas) {
                    if (view.text.isNotBlank()) {
                        val color = Color(0x777777u)
                        when {
                            model.pointerOver -> canvas.text(underLine { color { font(view)(view.text) } }, textPosition(view))
                            else              -> canvas.text(view.text, font(view), textPosition(view), ColorFill(color))
                        }
                    }
                }
            }
            fired += { dataStore.removeCompleted() }
        }

        updateLabel(label)
        clearAll.visible = dataStore.completed.isNotEmpty()
        visible = !dataStore.isEmpty

        dataStore.tasksChanged += {
            visible = !it.isEmpty
            updateLabel(label)
            clearAll.visible = it.completed.isNotEmpty()
        }

        height    = 41.0
        children += listOf(label, all, active, completed, clearAll)

        layout = constrain(label, all, active, completed, clearAll) { label, all_, active_, completed_, clearAll ->
            val spacing = 6.0

            listOf(label, all_, active_, completed_, clearAll).forEach { it.centerY = parent.centerY }

            label.left      = parent.left  + 15
            clearAll.right  = parent.right
            clearAll.height = parent.height

            all_.left       = parent.left + (parent.width - { all.width + active.width + completed.width + spacing * 2 }) / 2
            active_.left    = all_.right    + spacing
            completed_.left = active_.right + spacing
        }
    }

    override fun render(canvas: Canvas) {
        canvas.line(Point(y = 0.5), Point(width, 0.5), Stroke(Color(0xEDEDEDu)))
    }
}

class Todo(focusManager: FocusManager, textMetrics: TextMetrics, config: TodoConfig, model: DataStoreListModel): View() {
    init {
        children += Label("todos").apply {
            font            = config.largeFont
            foregroundColor = Color(0xAF2F2FU) opacity 0.15f
            acceptsThemes   = false
            behavior        = CommonLabelBehavior(textMetrics)
        }

        children += object: View() {
            init {
                width              = 550.0
                clipCanvasToBounds = false

                val visualizer = itemVisualizer<Task, IndexedIem> { item, previous, _ ->
                    when (previous) {
                        is TaskView -> previous.also { it.task = item }
                        else        -> TaskView(item, model, config.checkBackground, config.checkImage)
                    }
                }

                val list = MutableList(model, itemVisualizer = visualizer).apply {
                    val rowHeight = 58.0
                    font          = config.mediumFont
                    cellAlignment = fill
                    behavior      = BasicListBehavior(
                            focusManager,
                            basicItemGenerator {
                                pointerChanged += released {
                                    if (it.clickCount >= 2) {
                                        (list as? MutableList<Task, *>)?.startEditing(index)
                                        it.consume()
                                    }
                                }
                            },
                            PatternFill(Size(10.0, rowHeight)) {
                                line(Point(0.0, 1.0), Point(10.0, 1.0), Stroke(Color(0xEDEDEDu)))
                            },
                            rowHeight)

                    editor = listEditor { list, row, _, current -> TaskEditOperation(focusManager, list, row, current) }
                }

                children += CreationBox(focusManager, config.mediumFont, config.italicFont, model)
                children += list
                children += FilterBox(textMetrics, model, config.smallFont)

                layout = ListLayout(widthSource = Parent) then {
                    height = children.last { it.visible }?.bounds?.bottom ?: 0.0
                }
            }

            override fun render(canvas: Canvas) {
                canvas.outerShadow(vertical =  2.0, blurRadius =  4.0, color = Black opacity 0.2f) {
                    outerShadow   (vertical = 25.0, blurRadius = 50.0, color = Black opacity 0.1f) {
                        rect(bounds.atOrigin, color = White)
                    }
                }
            }
        }

        children += FooterInfo(textMetrics, config)

        layout = constrain(children[0], children[1], children[2]) { header, contents, footer ->
            header.top       = parent.top + 9
            header.centerX   = parent.centerX
            contents.top     = header.bottom + 5
            contents.centerX = parent.centerX
            footer.top       = contents.bottom + 65
            footer.centerX   = parent.centerX
            footer.width     = contents.width
        }
    }
}

class TodoApp(display        : Display,
              fonts          : FontLoader,
              images         : ImageLoader,
              persistentStore: PersistentStore,
              focusManager   : FocusManager,
              textMetrics    : TextMetrics,
              themes         : ThemeManager,
              theme          : DynamicTheme): Application {
    init {
        GlobalScope.launch {
            val largeFont = fonts {
                size   = 100
                weight = 100
                family = "Helvetica Neue"
            }

            val mediumFont     = fonts(largeFont ) { size   = 24     }
            val smallFont      = fonts(largeFont ) { size   = 14     }
            val italicFont     = fonts(mediumFont) { style  = Italic }
            val footerFont     = fonts(largeFont ) { size   = 10     }
            val boldFooterFont = fonts(footerFont) { weight = 400    }

            val toggleBackground = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E")
            val selectedImage    = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E")

            themes.selected = theme

            val tasks = SimpleDataStore(persistentStore.loadTasks()).apply {
                tasksChanged += {
                    persistentStore.save(tasks)
                }
            }

            display += Todo(focusManager, textMetrics, TodoConfig(largeFont, mediumFont, smallFont, italicFont, footerFont, boldFooterFont, toggleBackground!!, selectedImage!!), tasks)
            display.layout = constrain(display.children[0]) { fill(it) }
            display.fill(ColorFill(Color(0xF5F5F5u)))
        }
    }

    override fun shutdown() {}
}