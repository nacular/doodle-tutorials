package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.Photo
import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.icons.ImageIcon
import io.nacular.doodle.controls.table.AbstractTableBehavior.HeaderCellGenerator
import io.nacular.doodle.controls.table.AbstractTableBehavior.HeaderPositioner
import io.nacular.doodle.controls.table.CellInfo
import io.nacular.doodle.controls.table.CellVisualizer
import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.controls.table.ColumnSizePolicy
import io.nacular.doodle.controls.table.DynamicTable
import io.nacular.doodle.controls.table.HeaderGeometry
import io.nacular.doodle.controls.table.Table
import io.nacular.doodle.controls.table.TableBehavior
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.height
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.text
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.VerticalListPositioner
import io.nacular.doodle.theme.basic.table.BasicCellGenerator
import io.nacular.doodle.utils.ObservableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.min

data class Contact(val name: String, val phoneNumber: String)

private interface PhoneBookModel {
    operator fun plusAssign (contact: Contact)
    operator fun minusAssign(contact: Contact)
}

private class PhoneBookModelImpl: SimpleMutableListModel<Contact>(ObservableList()), PhoneBookModel {
    override fun plusAssign(contact: Contact) {
        super.add(contact)
    }

    override fun minusAssign(contact: Contact) {
        super.remove(contact)
    }
}

/**
 * Renders the header portion of the main page
 */
private class Header(
    private val textMetrics   : TextMetrics,
    private val addImage      : Image,
    private val largeFont     : Font,
    private val smallFont     : Font,
                phoneBookModel: PhoneBookModel,
                logo          : Image
): View() {
    private inner class CreateContactButton: PushButton("Create Contact", ImageIcon(addImage)) {
        init {
            size            = Size(186, 45)
            font            = smallFont
            acceptsThemes   = false
            iconTextSpacing = 10.0

            behavior = object: CommonTextButtonBehavior<Button>(textMetrics) {
                override fun clipCanvasToBounds(view: Button) = false

                override fun render(view: Button, canvas: Canvas) {
                    val draw = {
                        canvas.rect(
                            bounds.atOrigin,
                            radius = view.height / 2,
                            fill   = White.paint,
                        )
                    }

                    canvas.outerShadow(horizontal = 0.0, vertical = 0.0, color = Black.opacity(0.1f), blurRadius = 3.0) {
                        when {
                            view.model.pointerOver && !view.model.pressed -> canvas.outerShadow(horizontal = 0.0, vertical = 3.0, color = Black.opacity(0.1f), blurRadius = 3.0) {
                                draw()
                            }
                            else -> draw()
                        }
                    }

                    icon!!.render(view, canvas, at = iconPosition(view, icon = icon!!) + Point(x = 10))
                    canvas.text  (view.text,    at = textPosition(view, icon = icon  ) + Point(x =  8), font = view.font, color = Black)
                }
            }
        }
    }

    init {
        height = PAGE_HEADER_HEIGHT

        children += Photo(logo).apply { size = Size(40) }
        children += Label("Phonebook").apply { font = largeFont }

        // TODO: Add search/filter

        children += CreateContactButton() // Will use modal to show creation form

        layout = constrain(children[0], children[1], children[2]) { logo, label, button ->
            logo.top       = parent.top + 10
            logo.left      = parent.left + INSET
            label.left     = logo.right + 10
            label.centerY  = logo.centerY
            button.right   = parent.right - INSET
            button.centerY = logo.centerY
        }
    }
}

/**
 * Renders the Avatar and name for the name column
 */
private class NameCell(private val textMetrics: TextMetrics, value: String): View() {
    init {
        update(value)
    }

    fun update(value: String) {
        layout = null
        children.clear()
        children += Label("${value.first()}").apply {
            val circleColor = Red // TODO: Random color

            size            = Size(36)
            fitText         = emptySet()
            acceptsThemes   = false
            foregroundColor = blackOrWhiteContrast(circleColor)
            behavior        = object: CommonLabelBehavior(textMetrics) {
                override fun render(view: Label, canvas: Canvas) {
                    canvas.circle(Circle(radius = min(width, height) / 2, center = Point(width / 2, height / 2)), fill = circleColor.paint)
                    super.render(view, canvas)
                }
            }
        }

        children += Label(value)

        layout = constrain(children[0], children[1]) { icon, name ->
            icon.centerY = parent.centerY
            name.left    = icon.right + INSET
            name.centerY = icon.centerY
        }.then {
            size = Size(children[1].bounds.right, children[0].height)
        }
    }
}

class PhoneBookApp(
                display     : Display,
                fonts       : FontLoader,
                images      : ImageLoader,
                focusManager: FocusManager,
                themeManager: ThemeManager,
                theme       : DynamicTheme,
                Modal       : ModalFactory,
                uiDispatcher: CoroutineDispatcher,
    private val textMetrics : TextMetrics): Application {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        appScope.launch(uiDispatcher) {
            themeManager.selected = theme

            val largeFont = fonts("dmsans.woff2") {
                size     = 20
                weight   = 100
                families = listOf("DM Sans", "sans-serif")
            }

            val smallFont = fonts(largeFont!!) {
                size = 16
            }

            val model = PhoneBookModelImpl()

            // Test Data
            model += Contact("Joe", "1234567")
            model += Contact("Jack", "1234567")
            model += Contact("Bob", "1234567")
            model += Contact("Jen", "1234567")
            model += Contact("Herman", "1234567")
            model += Contact("Lisa Fuentes", "1234567")
            model += Contact("Langston Hughes", "1234567")

            display += Header(
                logo           = images.load("logo.png")!!,
                addImage       = images.load("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACQAAAAkCAYAAADhAJiYAAAAAXNSR0IArs4c6QAAAL5JREFUWEdj/P///3+GQQQYRx1EIDZGQ4hQch1+IfTaxQzF06J7ThEKBLzyFIfQqIMIhf9oCI2GEKV12WgaGjRp6M9eNkJuAcu/bzeAq/trwM8QLbSBKH17q7ixqsNZUpPqIFIcA3IJTR30WpORIVdyH1EhA1NEMwfd2mBIsmPICiFivWu6MgRF6enwNcRqJS0NEWvqqIMIhdRoCI2GEKW1/WgaGnJpiJCDSZWnuF9GqoWE1I86aDSECIUAIXkAXtfmleN2uj8AAAAASUVORK5CYII=")!!,
                textMetrics    = textMetrics,
                phoneBookModel = model,
                largeFont      = largeFont,
                smallFont      = smallFont!!
            ).apply { font = largeFont }

            display += DynamicTable<Contact, MutableListModel<Contact>>(model) {
                val alignment: Constraints.() -> Unit = {
                    left    = parent.left + INSET
                    centerY = parent.centerY
                }

                val nameVisualizer = object: CellVisualizer<String> {
                    override fun invoke(item: String, previous: View?, context: CellInfo<String>): View = when (previous) {
                        is NameCell -> previous.also { it.update(item) }
                        else        -> NameCell(textMetrics, item)
                    }
                }

                column(Label("Name"        ), { name        }, nameVisualizer   ) { width = 300.0; cellAlignment = alignment; headerAlignment = alignment }
                column(Label("Phone Number"), { phoneNumber }, TextVisualizer() ) { cellAlignment = alignment; headerAlignment = alignment }
            }.apply {
                font = smallFont

                if (false) {
                    // FIXME: There is a bug in Doodle where this triggers a layout before the behavior is installed, leading to an index out of bounds
                    // This will be fixed in 0.7.2

                    // "un-comment" to get policy where each column is equally sized
                    columnSizePolicy = object : ColumnSizePolicy {
                        override fun layout(width: Double, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
                            val colWidth = width / (columns.size - 1)

                            columns.forEach {
                                it.width = colWidth
                            }

                            return width
                        }

                        override fun widthChanged(width: Double, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
                            // no-op
                        }
                    }
                }

                acceptsThemes = false

                behavior      = object: TableBehavior<Contact>() {
                    override val headerCellGenerator = object: HeaderCellGenerator<Table<Contact, *>> {
                        override fun <A> invoke(table: Table<Contact, *>, column: Column<A>) = container {
                            column.header?.let { header ->
                                this += header
                                column.headerAlignment?.let { alignment ->
                                    layout = constrain(header) {
                                        alignment(it)
                                    }
                                }
                            }

                            val thickness = 1.0
                            render = {
                                line(Point(0.0, height - thickness), Point(width, height - thickness), stroke = Stroke(thickness = thickness, fill = Color(229u, 231u, 235u).paint))
                            }
                        }
                    }

                    override val headerPositioner = object: HeaderPositioner<Table<Contact, *>> {
                        override fun invoke(table: Table<Contact, *>) = HeaderGeometry(0.0, TABLE_HEADER_HEIGHT)
                    }

                    override val overflowColumnConfig = null

                    // TODO: Replace with generator that provides hover, edit, delete
                    override val cellGenerator: CellGenerator<Contact> = BasicCellGenerator()

                    override val rowPositioner: RowPositioner<Contact> = object: RowPositioner<Contact> {
                        private val delegate = VerticalListPositioner(ROW_HEIGHT)

                        override fun rowBounds  (of: Table<Contact, *>, row: Contact, index: Int) = delegate.itemBounds (of.size,     of.insets, index)
                        override fun row        (of: Table<Contact, *>, at: Point               ) = delegate.itemFor    (of.size,     of.insets,  at  )
                        override fun minimumSize(of: Table<Contact, *>                          ) = delegate.minimumSize(of.numItems, of.insets       )
                    }
                }
            }

            display.layout = constrain(display.children[0], display.children[1]) { header, table ->
                header.left  = parent.left + INSET
                header.right = parent.right
                table.top    = header.bottom
                table.left   = header.left
                table.right  = header.right - INSET
                table.bottom = parent.bottom
            }
        }
    }

    override fun shutdown() { /* no-op */ }
}

private const val PAGE_HEADER_HEIGHT  = 64.0
private const val TABLE_HEADER_HEIGHT = 50.0
private const val ROW_HEIGHT          = 60.0
private const val INSET               = 16.0
