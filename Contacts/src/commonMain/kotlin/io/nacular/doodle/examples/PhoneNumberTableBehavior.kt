package io.nacular.doodle.examples

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.table.AbstractTableBehavior
import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.controls.table.HeaderGeometry
import io.nacular.doodle.controls.table.Table
import io.nacular.doodle.controls.table.TableBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.height
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.basic.VerticalListPositioner
import io.nacular.doodle.utils.SetObserver

class PhoneNumberTableBehavior: TableBehavior<Contact>() {
    private inner class ContactRow<T>(private val column: Column<T>, table: Table<Contact, *>, cell: T, row: Int, private val itemVisualizer: ItemVisualizer<T, IndexedItem>) : View() {
        private var index = row

        init {
            children += itemVisualizer(cell, context = SimpleIndexedItem(row, false))

            focusable       = false
            styleChanged   += { rerender() }
            pointerChanged += object: PointerListener {
                private var pressed = false
                private var pointerOver = false

                override fun entered(event: PointerEvent) {
                    pointerOver = true
                    table.addSelection(setOf(index))
                }

                override fun exited(event: PointerEvent) {
                    parent?.toLocal(event.location, from = event.target)?.let { point ->
                        if (!contains(point)) {
                            pointerOver = false
                            table.removeSelection(setOf(index))
                        }
                    }
                }

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

    override fun install(view: Table<Contact, *>) {
        view.selectionChanged += selectionChanged
    }

    override fun uninstall(view: Table<Contact, *>) {
        view.selectionChanged -= selectionChanged
    }

    override val headerCellGenerator = object: AbstractTableBehavior.HeaderCellGenerator<Table<Contact, *>> {
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

    override val headerPositioner = object: AbstractTableBehavior.HeaderPositioner<Table<Contact, *>> {
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
        override fun row        (of: Table<Contact, *>, at: Point) = delegate.itemFor    (of.size,     of.insets,  at  )
        override fun minimumSize(of: Table<Contact, *>) = delegate.minimumSize(of.numItems, of.insets       )
    }

    override fun renderBody(table: Table<Contact, *>, canvas: Canvas) {
        table.selection.map { it to table[it] }.forEach { (index, row) ->
            row.onSuccess {
                canvas.rect(rowPositioner.rowBounds(table, it, index).inset(Insets(top = 1.0)), Color(0xf5f5f5u))
            }
        }
    }
}

private const val ROW_HEIGHT          = 60.0
private const val TABLE_HEADER_HEIGHT = 50.0