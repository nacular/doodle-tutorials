package io.nacular.doodle.examples

import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.table.CellInfo
import io.nacular.doodle.controls.table.CellVisualizer
import io.nacular.doodle.controls.table.ColumnSizePolicy
import io.nacular.doodle.controls.table.DynamicTable
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


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
            val circleColor = value.toColor()

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

/**
 * Renders edit/delete buttons within a row when it is selected
 */
private class ToolCell(private val pathMetrics: PathMetrics, private var selected: Boolean): View() {

    init {
        update(selected)
    }

    private fun createButton(path: String) = PathIconButton(
        pathData    = path,
        pathMetrics = pathMetrics
    ).apply {
        size            = Size(24)
        foregroundColor = Black.opacity(0.3f)
        pointerChanged  += object: PointerListener {
            override fun entered(event: PointerEvent) {
                foregroundColor = Black
            }

            override fun exited(event: PointerEvent) {
                foregroundColor = Black.opacity(0.3f)
            }
        }
    }

    var onEdit  : (() -> Unit)? = null
    var onDelete: (() -> Unit)? = null

    fun update(selected: Boolean) {
        if (selected == this.selected) return

        this.selected = selected

        when {
            selected -> {
                children += createButton(EDIT_ICON_PATH  ).apply {
                    fired += {
                        onEdit?.invoke()
                    }
                }
                children += createButton(DELETE_ICON_PATH).apply {
                    fired += {
                        onDelete?.invoke()
                    }
                }

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

class ContactList(
    fonts      : AppFonts,
    modals     : Modals,
    appScope   : CoroutineScope,
    contacts   : MutableListModel<Contact>,
    navigator  : Navigator,
    textMetrics: TextMetrics,
    pathMetrics: PathMetrics,
): DynamicTable<Contact, MutableListModel<Contact>>(contacts, SingleItemSelectionModel(), block = {
    val alignment: Constraints.() -> Unit = {
        left    = parent.left + INSET
        centerY = parent.centerY
    }

    val nameVisualizer = object: CellVisualizer<Contact, String> {
        override fun invoke(item: String, previous: View?, context: CellInfo<Contact, String>) = when (previous) {
            is NameCell -> previous.also { it.update(item) }
            else        -> NameCell(textMetrics, item)
        }
    }

    val toolsVisualizer = object: CellVisualizer<Contact, Unit> {
        override fun invoke(item: Unit, previous: View?, context: CellInfo<Contact, Unit>) = when (previous) {
            is ToolCell -> previous.also { it.update(context.selected) }
            else        -> ToolCell(pathMetrics, context.selected).apply {
                onDelete = {
                    appScope.launch {
                        if (modals.delete(context.item, fonts).show()) {
                            navigator.contactDeleted(context.item)
                        }
                    }
                }
                onEdit = {
                    navigator.showContactEdit(context.item)
                }
            }
        }
    }

    column(Label("Name"        ), { name        }, nameVisualizer   ) { width = 300.0; cellAlignment = alignment; headerAlignment = alignment }
    column(Label("Phone Number"), { phoneNumber }, TextVisualizer() ) { cellAlignment = alignment; headerAlignment = alignment                }
    column(null,                                   toolsVisualizer  ) { cellAlignment = fill(Insets(top = 20.0, bottom = 20.0, right = 20.0)); width = 100.0; maxWidth = 100.0 }

}) {
    init {
        if (true) {
            // FIXME: There is a bug in Doodle where this triggers a layout before the behavior is installed, leading to an index out of bounds
            // This will be fixed in 0.7.2

            // "un-comment" to get policy where each column is equally sized
            columnSizePolicy = object: ColumnSizePolicy {
                override fun layout(width: Double, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
                    columns[0].width = width / 2
                    columns[1].width = max(300.0, width / 2 - 100)
                    columns[2].width = width - columns[0].width - columns[1].width

                    return width
                }

                override fun widthChanged(width: Double, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
                    // no-op
                }
            }
        }

        acceptsThemes = false
        behavior      = ContactListBehavior(navigator)
    }
}

private /*const*/ val EDIT_ICON_PATH   = if (DESKTOP_WORK_AROUND) "M1 16c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V4H1v12zM14 1h-3.5l-1-1h-5l-1 1H0v2h14V1z" else "M0 18h3.75L14.81 6.94l-3.75-3.75L0 14.25V18zm2-2.92 9.06-9.06.92.92L2.92 16H2v-.92zM15.37.29a.996.996 0 00-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83a.996.996 0 000-1.41l-2.34-2.34z"
private const val DELETE_ICON_PATH = "M1 16c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V4H1v12zM14 1h-3.5l-1-1h-5l-1 1H0v2h14V1z"