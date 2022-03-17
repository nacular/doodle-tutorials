package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.Photo
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.icons.ImageIcon
import io.nacular.doodle.controls.icons.PathIcon
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
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.controls.theme.simpleButtonRenderer
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.height
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.drawing.text
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.path
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.VerticalListPositioner
import io.nacular.doodle.utils.Anchor.Leading
import io.nacular.doodle.utils.FilteredList
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.min


data class Contact(val name: String, val phoneNumber: String)

private interface PhoneBookModel {
    var filter: ((Contact) -> Boolean)?

    operator fun plusAssign (contact: Contact)
    operator fun minusAssign(contact: Contact)
}

private class PhoneBookModelImpl(
    private val contacts: ObservableList<Contact> = ObservableList(),
    private val filteredList: FilteredList<Contact> = FilteredList(contacts)
): SimpleMutableListModel<Contact>(filteredList), PhoneBookModel {
    override var filter: ((Contact) -> Boolean)? by observable(null) { _,new ->
        filteredList.filter = new
    }

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
    private val pathMetrics   : PathMetrics,
    private val addImage      : Image,
    private val largeFont     : Font,
    private val mediumFont    : Font,
    private val smallFont     : Font,
    private val phoneBookModel: PhoneBookModel,
                logo          : Image,
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
                        canvas.rect(bounds.atOrigin, radius = view.height / 2, color = White)
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

    private inner class FilterBox: View() {
        val path     = path("M12.5 11H11.71L11.43 10.73A6.471 6.471 0 0013 6.5 6.5 6.5 0 106.5 13C8.11 13 9.59 12.41 10.73 11.43L11 11.71V12.5L16 17.49 17.49 16 12.5 11ZM6.5 11C4.01 11 2 8.99 2 6.5S4.01 2 6.5 2 11 4.01 11 6.5 8.99 11 6.5 11Z")
        val pathSize = pathMetrics.size(path)

        private val textField = TextField().apply {
            font             = mediumFont
            placeHolder      = "Search"
            borderVisible    = false
            backgroundColor  = Transparent
            placeHolderColor = Color(156u, 153u, 155u)
            focusChanged    += { _,_,_ ->
                this@FilterBox.rerender()
            }
        }

        init {
            clipCanvasToBounds = false

            val clearButton = PushButton(
                icon = PathIcon(
                    path        = path("M14 1.41 12.59 0 7 5.59 1.41 0 0 1.41 5.59 7 0 12.59 1.41 14 7 8.41 12.59 14 14 12.59 8.41 7Z"),
                    fill        = Color(0x5f6368u),
                    pathMetrics = pathMetrics
                ),
            ).apply {
                size          = Size(22, 44)
                visible       = textField.text.isNotBlank()
                iconAnchor    = Leading
                acceptsThemes = false
                behavior      = simpleButtonRenderer { button, canvas ->
                    button.icon?.apply {
                        val iconSize = size(button)
                        render(button, canvas, at = Point((button.width - iconSize.width) / 2, (button.height - iconSize.height) / 2))
                    }
                }
                fired += {
                    textField.text = ""
                }
            }

            textField.textChanged += { _,_,new ->
                when {
                    new.isBlank() -> phoneBookModel.filter = null
                    else          -> phoneBookModel.filter = { it.name.contains(new, ignoreCase = true) }
                }

                clearButton.visible = new.isNotBlank()
            }

            children += textField
            children += clearButton

            layout = constrain(children[0], children[1]) { textField, clear ->
                textField.left    = parent.left + pathSize.width + 2 * 20
                textField.height  = parent.height
                textField.right   = clear.left
                textField.centerY = parent.centerY
                clear.right       = parent.right - 20
                clear.centerY     = parent.centerY
            }
        }

        override fun render(canvas: Canvas) {
            when {
                textField.hasFocus -> canvas.outerShadow(horizontal = 0.0, vertical = 0.0, color = Black.opacity(0.1f), blurRadius = 3.0) {
                    canvas.rect(bounds.atOrigin, radius = 8.0, color = White)
                }
                else               -> canvas.rect(bounds.atOrigin, radius = 8.0, color = Color(241u, 243u, 244u))
            }

            canvas.transform(Identity.translate(20.0, (height - pathSize.height) / 2)) {
                canvas.path(path, fill = Color(0x5f6368u).paint)
            }
        }
    }

    init {
        height = PAGE_HEADER_HEIGHT

        children += Photo              (logo       ).apply { size = Size(40)  }
        children += Label              ("Phonebook").apply { font = largeFont }
        children += FilterBox          (           ).apply { width = 300.0    }

        // FIXME: Creation button needs to float in display to work with small screens
        children += CreateContactButton(           ) // Will use modal to show creation form

        layout = constrain(children[0], children[1], children[2], children[3]) { logo, label, filter, button ->
            logo.top       = parent.top + 10
            logo.left      = parent.left + INSET
            label.left     = logo.right + 10
            label.centerY  = logo.centerY
            filter.height  = button.height
            filter.centerX = parent.centerX
            filter.centerY = logo.centerY
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

    // Simple string to color mapper from: https://github.com/zenozeng/color-hash/blob/main/lib/bkdr-hash.ts
    private fun toColor(value: String): Color {
        val seed           = 131
        val seed2          = 237
        var hash           = 0
        val str            = value + 'x' // make hash more sensitive for short string like 'a', 'b', 'c'
        val maxSafeInteger = floor((0xffffffu / seed2.toUInt()).toDouble())

        str.forEach {
            if (hash > maxSafeInteger) {
                hash = floor((hash / seed2).toDouble()).toInt()
            }
            hash = hash * seed + it.code
        }

        return Color(hash.toUInt())
    }

    fun update(value: String) {
        layout = null
        children.clear()
        children += Label("${value.first()}").apply {
            val circleColor = toColor(value) // TODO: Random color

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

private class ToolCell(selected: Boolean): View() {

    init {
        update(selected)
    }

    fun update(selected: Boolean) {
        when {
            selected -> {
                children += PushButton("E").apply { size = Size(24) }
                children += PushButton("D").apply { size = Size(24) }

                layout = constrain(children[0], children[1]) { edit, delete ->
                    delete.centerY = parent.centerY
                    delete.right   = parent.right
                    edit.centerY   = delete.centerY
                    edit.right     = delete.left - 4
                }
            }
            else     -> {
                layout = null
                children.clear()
            }
        }
    }
}

private class PhoneNumberTableBehavior: TableBehavior<Contact>() {
    private inner class ContactRow<T>(private val column: Column<T>, table: Table<Contact, *>, cell: T, row: Int, private val itemVisualizer: ItemVisualizer<T, IndexedItem>) : View() {
        private var index = row

        init {
            children += itemVisualizer(cell, context = SimpleIndexedItem(row, false))

            focusable       = false
            styleChanged   += { rerender() }
            pointerChanged += object: PointerListener {
                private var pressed = false
                private var pointerOver = false

                override fun pressed(event: PointerEvent) {
                    pressed = true
                }

                override fun released(event: PointerEvent) {
                    if (pointerOver && pressed) {

                    }
                    pressed = false
                }
            }

            update(table, cell, row)
        }

        fun update(table: Table<Contact, *>, cell: T, row: Int) {
            index       = row
            children[0] = itemVisualizer(cell, children.firstOrNull(), SimpleIndexedItem(row, table.selected(index)))

            idealSize = children[0].idealSize
            column.cellAlignment?.let { alignment ->
                layout = constrain(children[0]) {
                    alignment(it)
                }
            }
        }

        override fun render(canvas: Canvas) {
            backgroundColor?.let { canvas.rect(bounds.atOrigin, it.paint) }
        }
    }

    private val selectionChanged: SetObserver<Table<Contact, *>, Int> = { table,_,_ ->
        table.bodyDirty()
    }

    private fun Table<*,*>.toTableBody(point: Point, from: View) = toLocal(point, from).run { Point(x, y - TABLE_HEADER_HEIGHT) }

    // FIXME: Improve exit handling to remove selection
    private val pointerListener = object: PointerListener {
        override fun exited(event: PointerEvent) {
            (event.source as? Table<*,*>)?.let { table ->
                if (table.toTableBody(event.location, from = event.target) !in table) {
                    table.clearSelection()
                }
            }
        }
    }

    // FIXME: Ignore header
    @Suppress("UNCHECKED_CAST")
    private val pointerMotionListener = object: PointerMotionListener {
        override fun moved(event: PointerEvent) {
            (event.source as? Table<Contact,*>)?.let {
                it.setSelection(setOf(rowPositioner.row(of = it, it.toTableBody(event.location, from = event.target))))
            }
        }
    }

    override fun install(view: Table<Contact, *>) {
        view.pointerFilter       += pointerListener
        view.selectionChanged    += selectionChanged
        view.pointerMotionFilter += pointerMotionListener
    }

    override fun uninstall(view: Table<Contact, *>) {
        view.pointerFilter       -= pointerListener
        view.selectionChanged    -= selectionChanged
        view.pointerMotionFilter -= pointerMotionListener
    }

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
    override val cellGenerator: CellGenerator<Contact> = object: CellGenerator<Contact> {
        override fun <A> invoke(table: Table<Contact, *>, column: Column<A>, cell: A, row: Int, itemGenerator: ItemVisualizer<A, IndexedItem>, current: View?): View = when (current) {
            is ContactRow<*> -> (current as ContactRow<A>).apply { update(table, cell, row) }
            else             -> ContactRow(column, table, cell, row, itemGenerator)
        }
    }

    override val rowPositioner: RowPositioner<Contact> = object: RowPositioner<Contact> {
        private val delegate = VerticalListPositioner(ROW_HEIGHT)

        override fun rowBounds  (of: Table<Contact, *>, row: Contact, index: Int) = delegate.itemBounds (of.size,     of.insets, index)
        override fun row        (of: Table<Contact, *>, at: Point               ) = delegate.itemFor    (of.size,     of.insets,  at  )
        override fun minimumSize(of: Table<Contact, *>                          ) = delegate.minimumSize(of.numItems, of.insets       )
    }

    override fun renderBody(table: Table<Contact, *>, canvas: Canvas) {
        table.selection.map { it to table[it] }.forEach { (index, row) ->
            row.onSuccess {
                canvas.rect(rowPositioner.rowBounds(table, it, index).inset(Insets(top = 1.0)), Color(0xf5f5f5u))
            }
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
    private val textMetrics : TextMetrics,
                pathMetrics : PathMetrics): Application {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        appScope.launch(uiDispatcher) {
            themeManager.selected = theme

            val largeFont = fonts("dmsans.woff2") {
                size     = 20
                weight   = 100
                families = listOf("DM Sans", "sans-serif")
            }!!

            val mediumFont = fonts(largeFont) { size = 18 }!!
            val smallFont  = fonts(largeFont) { size = 16 }!!

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
                addImage       = images.load("add.png" )!!,
                pathMetrics    = pathMetrics,
                textMetrics    = textMetrics,
                phoneBookModel = model,
                largeFont      = largeFont,
                smallFont      = smallFont,
                mediumFont     = mediumFont
            ).apply { font = largeFont }

            display += DynamicTable<Contact, MutableListModel<Contact>>(model, SingleItemSelectionModel()) {
                val alignment: Constraints.() -> Unit = {
                    left    = parent.left + INSET
                    centerY = parent.centerY
                }

                val nameVisualizer = object: CellVisualizer<String> {
                    override fun invoke(item: String, previous: View?, context: CellInfo<String>) = when (previous) {
                        is NameCell -> previous.also { it.update(item) }
                        else        -> NameCell(textMetrics, item)
                    }
                }

                val toolsVisualizer = object: CellVisualizer<Unit> {
                    override fun invoke(item: Unit, previous: View?, context: CellInfo<Unit>) = when (previous) {
                        is ToolCell -> previous.also { it.update(context.selected) }
                        else        -> ToolCell(context.selected)
                    }
                }

                column(Label("Name"        ), { name        }, nameVisualizer   ) { width = 300.0; cellAlignment = alignment; headerAlignment = alignment }
                column(Label("Phone Number"), { phoneNumber }, TextVisualizer() ) { cellAlignment = alignment; headerAlignment = alignment                }
                column(null,                                   toolsVisualizer  ) { cellAlignment = fill; width = 54.0; maxWidth = 54.0                                                                      }
            }.apply {
                font = smallFont

                if (false) {
                    // FIXME: There is a bug in Doodle where this triggers a layout before the behavior is installed, leading to an index out of bounds
                    // This will be fixed in 0.7.2

                    // "un-comment" to get policy where each column is equally sized
                    columnSizePolicy = object: ColumnSizePolicy {
                        override fun layout(width: Double, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
                            val colWidth = (width - 54) / (columns.size - 2)

                            columns[0].width = width / 2
                            columns[1].width = width / 2 - 54
                            columns[2].width = 54.0

                            return width
                        }

                        override fun widthChanged(width: Double, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
                            // no-op
                        }
                    }
                }

                acceptsThemes = false
                behavior      = PhoneNumberTableBehavior()
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
