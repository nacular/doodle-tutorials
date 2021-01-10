package io.nacular.doodle.tutorials

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.MultiSelectionModel
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.theme.CommonButtonBehavior
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontDetector
import io.nacular.doodle.drawing.PatternFill
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.poly
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.event.KeyCode.Companion.Enter
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.BasicListBehavior
import io.nacular.doodle.utils.HorizontalAlignment.Left
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreationBox(private val focusManager: FocusManager, font: Font, list: MutableList<TaskData, *>, model: MutableListModel<TaskData>): View() {
    init {
        val color = Color(red = 77u, green = 77u, blue = 77u)

        val button = PushButton().apply {
            cursor = Default
            width  = 60.0
            fired += { when {
                list.selection.size < list.numRows -> list.selectAll     ()
                else                               -> list.clearSelection()
            } }
            visible   = !list.isEmpty
            focusable = false
            behavior  = object: CommonButtonBehavior<PushButton>() {
                override fun render(view: PushButton, canvas: Canvas) {
                    val thickness = 4.0
                    val polyBounds = view.bounds.atOrigin.inset(Insets(top = 27.0, bottom = 27.0, left = 22.0, right = 22.0))

                    val poly = ConvexPolygon(
                        Point(polyBounds.center.x, polyBounds.bottom),
                        Point(polyBounds.x, polyBounds.y + thickness),
                        polyBounds.position,
                        Point(polyBounds.center.x, polyBounds.bottom - thickness),
                        Point(polyBounds.right, polyBounds.y),
                        Point(polyBounds.right, polyBounds.y + thickness)
                    )

                    val color = when (list.selection.size) {
                        list.numRows -> Color(0x737373u)
                        else         -> Color(0xe6e6e6u)
                    }

                    canvas.poly(poly, color = color)
                }
            } as Behavior<Button>
        }

        children += button
        children += TextField().apply {
            this.font       = font
            borderVisible   = false
            foregroundColor = color
            placeHolder     = "What needs to be done?"

            keyChanged += object: KeyListener {
                override fun keyReleased(event: KeyEvent) {
                    when {
                        event.code == Enter && text.isNotBlank() -> {
                            list.add(TaskData(text))

                            text = ""
                        }
                    }
                }
            }
        }

        model.changed += { _,_,_,_ ->
            button.visible = !list.isEmpty
        }

        list.selectionChanged += { _,_,_ ->
            button.rerender()
        }

        layout = constrain(button, children[1]) { button, textField ->
            button.top       = parent.top
            button.bottom    = parent.bottom
            textField.top    = button.top
            textField.left   = button.right
            textField.right  = parent.right
            textField.bottom = button.bottom
        }
        cursor = Cursor.Text

        pointerChanged += object: PointerListener {
            override fun released(event: PointerEvent) {
                focusManager.requestFocus(children[1])
            }
        }
    }

    override fun addedToDisplay() {
        focusManager.requestFocus(children[1])
    }
}

data class TaskData(var text: String)

class Task(data: TaskData, model: MutableListModel<TaskData>, toggleBackground: Image, selectedImage: Image): View() {

    private var data = data
        set(new) {
            field = new
            label.text = new.text
        }

    private val check = CheckBox().apply {
        behavior = object: CommonButtonBehavior<CheckBox>() {
            override fun render(view: CheckBox, canvas: Canvas) {
                val destination = bounds.atOrigin.inset(Insets(left = 10.0, right = 10.0, top = 9.0, bottom = 9.0))

                when {
                    view.selected -> canvas.image(selectedImage, destination = destination)
                }

                canvas.image(toggleBackground, destination = destination)
            }
        } as Behavior<Button>
    }

    private val label = Label(data.text).apply {
        fitText             = emptySet()
        horizontalAlignment = Left
    }

    private val delete = PushButton().apply {
        visible    = false
        focusable  = false
        fired     += {
            model.remove(this@Task.data)
        }
        behavior = object: CommonButtonBehavior<PushButton>() {
            override fun render(view: PushButton, canvas: Canvas) {
                val thickness = 1.0
                val iconBounds = view.bounds.atOrigin.inset(Insets(top = 22.0, bottom = 22.0, left = 23.0, right = 23.0))

                val color = when {
                    view.model.pointerOver -> Color(0xaf5b5eu) // TODO: Animate instead
                    else                   -> Color(0xcc9a9au)
                }

                canvas.line(iconBounds.position, Point(iconBounds.right, iconBounds.bottom), Stroke(color, thickness))
                canvas.line(Point(iconBounds.right, iconBounds.y), Point(iconBounds.x, iconBounds.bottom), Stroke(color, thickness))
            }
        } as Behavior<Button>
    }

    fun setData(data: TaskData) {
        this.data = data

//        delete.visible = false
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
            override fun entered(event: PointerEvent) {
                children[2].visible = true
            }

            override fun exited(event: PointerEvent) {
                children[2].visible = false
            }
        }

        setData(data)
    }
}

class TaskVisualizer(
        private val model: MutableListModel<TaskData>,
        private val font: Font,
        private val toggleBackground: Image,
        private val selectedImage: Image
): ItemVisualizer<TaskData, IndexedIem> {
    override fun invoke(item: TaskData, previous: View?, context: IndexedIem) = when (previous) {
        is Task -> previous.also { it.setData(item) }
        else    -> Task(item, model, toggleBackground, selectedImage).also { it.font = font }
    }
}

data class TodoConfig(val largeFont: Font, val mediumFont: Font, val toggleBackground: Image, val selectedImage: Image)

class Todo(focusManager: FocusManager, textMetrics: TextMetrics, config: TodoConfig): View() {
    init {
        children += Label("todos").apply {
            font            = config.largeFont
            foregroundColor = Color(red = 175u, green = 47u, blue =  47u, opacity = 0.15f)
            acceptsThemes   = false
            behavior        = CommonLabelBehavior(textMetrics)
        }

        children += object: View() {
            init {
                size               = Size(550.0, 65.0)
                clipCanvasToBounds = false

                val listModel = SimpleMutableListModel<TaskData>()
                val list      = MutableList(listModel, selectionModel = MultiSelectionModel(), itemVisualizer = TaskVisualizer(listModel, config.mediumFont, config.toggleBackground, config.selectedImage)).apply {
                    behavior = object: BasicListBehavior<TaskData>(null, 58.0, null, null, null, null) {
                        override fun render(view: List<TaskData, *>, canvas: Canvas) {
                            canvas.rect(view.bounds.atOrigin, PatternFill(Size(10.0, 58.0)) {
                                line(Point(0.0, 1.0), Point(10.0, 1.0), Stroke(Color(0xedededu)))
                            })
                        }
                    }
                    cellAlignment = fill
                }

                children += CreationBox(focusManager, config.mediumFont, list, listModel)
                children += list

                val constraintLayout = constrain(children[0], list) { textBox, list ->
                    textBox.width  = parent.width
                    textBox.height = constant(65.0)
                    list.top       = textBox.bottom
                    list.width     = parent.width
                }

                layout = object: Layout {
                    override fun layout(container: PositionableContainer) {
                        constraintLayout.layout(container)

                        height = children[1].bounds.bottom
                    }
                }
            }

            override fun render(canvas: Canvas) {
                canvas.outerShadow(vertical = 2.0, blurRadius = 4.0, color = Black opacity 0.2f) {
                    canvas.outerShadow(vertical = 25.0, blurRadius = 60.0, color = Black opacity 0.1f) {
                        rect(bounds.atOrigin, color = White)
                    }
                }
            }
        }

        layout = constrain(children[0], children[1]) { header, contents ->
            header.centerX   = parent.centerX
            contents.top     = header.bottom
            contents.centerX = parent.centerX
        }
    }

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, color = Color(0xf5f5f5u))
    }
}

class TodoApp(
        display     : Display,
        imageLoader : ImageLoader,
        fontDetector: FontDetector,
        focusManager: FocusManager,
        textMetrics : TextMetrics,
        themeManager: ThemeManager,
        dynamicTheme: DynamicTheme
): Application {
    init {
        GlobalScope.launch {
            val largeFont = fontDetector {
                size   = 100
                weight = 100
                family = "Helvetica Neue"
            }

            val mediumFont = fontDetector(largeFont) {
                size = 24
            }

            val toggleBackground = imageLoader.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E")
            val selectedImage    = imageLoader.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E")

            themeManager.selected = dynamicTheme

            display += Todo(focusManager, textMetrics, TodoConfig(largeFont, mediumFont, toggleBackground!!, selectedImage!!))
            display.layout = constrain(display.children[0]) {
                fill(it)
            }
        }
    }

    override fun shutdown() {}
}