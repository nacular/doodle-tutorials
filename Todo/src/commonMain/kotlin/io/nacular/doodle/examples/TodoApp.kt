@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.*
import io.nacular.doodle.controls.buttons.*
import io.nacular.doodle.controls.list.*
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
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.*
import io.nacular.doodle.image.*
import io.nacular.doodle.layout.*
import io.nacular.doodle.layout.WidthSource.Parent
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.*
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment.Left
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

        children += button
        children += TextField().apply {
            this.font        = font
            borderVisible    = false
            foregroundColor  = color
            placeHolder      = "What needs to be done?"
            placeHolderColor = Color(0xE6E6E6u)
            this.placeHolderFont = placeHolderFont

            keyChanged += object: KeyListener {
                override fun keyReleased(event: KeyEvent) {
                    when {
                        event.code == KeyCode.Enter && text.isNotBlank() -> {
                            dataStore.add(text)

                            text = ""
                        }
                    }
                }
            }
        }

        dataStore.tasksChanged += {
            button.visible = it.size > 0
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

class TaskView(task: Task, dataStore: DataStore, toggleBackground: Image, selectedImage: Image): View() {
    var task = task
        set(new) {
            field          = new
            label.text     = new.text
            check.selected = field.completed
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

data class TodoConfig(val largeFont: Font, val mediumFont: Font, val italicFont: Font, val checkBackground: Image, val checkImage: Image)

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

                children += CreationBox(focusManager, config.mediumFont, config.italicFont, model).apply { height = 65.0 }
                children += list

                val listLayout = ListLayout(widthSource = Parent)

                layout = simpleLayout { container ->
                    listLayout.layout(container)

                    height = children[1].bounds.bottom
                }
            }

            override fun render(canvas: Canvas) {
                canvas.outerShadow(vertical =  2.0, blurRadius =  4.0, color = Black opacity 0.2f) {
                    outerShadow   (vertical = 25.0, blurRadius = 60.0, color = Black opacity 0.1f) {
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

            val mediumFont = fonts(largeFont ) { size  = 24     }
            val italicFont = fonts(mediumFont) { style = Italic }

            val toggleBackground = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E")
            val selectedImage    = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E")

            themes.selected = theme

            val tasks = SimpleDataStore(persistentStore.loadTasks()).apply {
                tasksChanged += {
                    persistentStore.save(tasks)
                }
            }

            display += Todo(focusManager, textMetrics, TodoConfig(largeFont, mediumFont, italicFont,  toggleBackground!!, selectedImage!!), tasks)
            display.layout = constrain(display.children[0]) { fill(it) }
            display.fill(ColorFill(Color(0xF5F5F5u)))
        }
    }

    override fun shutdown() {}
}