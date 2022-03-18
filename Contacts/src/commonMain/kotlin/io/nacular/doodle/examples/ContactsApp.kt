package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.icons.ImageIcon
import io.nacular.doodle.controls.table.CellInfo
import io.nacular.doodle.controls.table.CellVisualizer
import io.nacular.doodle.controls.table.ColumnSizePolicy
import io.nacular.doodle.controls.table.DynamicTable
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.drawing.text
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


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

    fun update(selected: Boolean) {
        if (selected == this.selected) return

        this.selected = selected

        when {
            selected -> {
                children += createButton(EDIT_ICON_PATH  )
                children += createButton(DELETE_ICON_PATH)

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

/**
 * Button use to create a new Contact
 */
private class CreateContactButton(textMetrics: TextMetrics, addImage: Image): PushButton("Create Contact", ImageIcon(addImage)) {
    init {
        val iconSize = icon!!.size(this)
        acceptsThemes   = false
        iconTextSpacing = 10.0
        behavior        = object: CommonTextButtonBehavior<Button>(textMetrics) {
            override fun clipCanvasToBounds(view: Button) = false

            override fun render(view: Button, canvas: Canvas) {
                val draw     = { canvas.rect(bounds.atOrigin, radius = view.height / 2, color = White) }
                val showText = view.width > view.height

                val shadow = when {
                    showText -> OuterShadow(horizontal = 0.0, vertical = 0.0, color = Black.opacity(0.1f), blurRadius = 3.0)
                    else     -> OuterShadow(horizontal = 0.0, vertical = 7.0, color = Black.opacity(0.1f), blurRadius = 5.0)
                }

                canvas.shadow(shadow) {
                    when {
                        view.model.pointerOver && !view.model.pressed -> canvas.outerShadow(horizontal = 0.0, vertical = 3.0, color = Black.opacity(0.1f), blurRadius = 3.0) {
                            draw()
                        }
                        else -> draw()
                    }
                }

                when {
                    showText -> {
                        icon!!.render(view, canvas, at = iconPosition(view, icon = icon!!) + Point(x = 10))
                        canvas.text(view.text, at = textPosition(view, icon = icon) + Point(x = 8), font = view.font, color = Black)
                    }
                    else     -> icon!!.render(view, canvas, at = Point((view.width - iconSize.width) / 2, (view.height - iconSize.height) / 2))
                }
            }
        }
    }
}

class ContactsApp<M>(
    display     : Display,
    uiDispatcher: CoroutineDispatcher,
    fonts       : FontLoader,
    images      : ImageLoader,
    focusManager: FocusManager,
    themeManager: ThemeManager,
    theme       : DynamicTheme,
    modals      : ModalFactory,
    textMetrics : TextMetrics,
    pathMetrics : PathMetrics,
    model       : M): Application where M: ContactsModel, M: MutableListModel<Contact> {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        appScope.launch(uiDispatcher) {
            themeManager.selected = theme

            val largeFont  = fonts("dmsans.woff2") {
                size     = 20
                weight   = 100
                families = listOf("DM Sans", "sans-serif")
            }!!
            val mediumFont = fonts(largeFont) { size = 18 }!!
            val smallFont  = fonts(largeFont) { size = 16 }!!

            display += Header(
                logoImage              = images.load("logo.png")!!,
                pathMetrics            = pathMetrics,
                contactsModel          = model,
                largeFont              = largeFont,
                mediumFont             = mediumFont,
                filterCenterAboveWidth = MEDIUM_WIDTH - 2 * INSET,
                filterRightAboveWidth  = SMALL_WIDTH  - 2 * INSET,
                naturalHeight          = PAGE_HEADER_HEIGHT,
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
                        else        -> ToolCell(pathMetrics, context.selected)
                    }
                }

                column(Label("Name"        ), { name        }, nameVisualizer   ) { width = 300.0; cellAlignment = alignment; headerAlignment = alignment }
                column(Label("Phone Number"), { phoneNumber }, TextVisualizer() ) { cellAlignment = alignment; headerAlignment = alignment                }
                column(null,                                   toolsVisualizer  ) { cellAlignment = fill(Insets(top = 20.0, bottom = 20.0, right = 20.0)); width = 100.0; maxWidth = 100.0 }
            }.apply {
                font = smallFont

                if (false) {
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
                behavior      = PhoneNumberTableBehavior()
            }

            display += CreateContactButton(textMetrics, images.load("add.png" )!!).apply { font = smallFont }

            display.layout = simpleLayout { container ->
                val header = container.children[0]
                val table  = container.children[1]
                val button = container.children[2]

                header.bounds = Rectangle(INSET, 0.0, container.width - 2 * INSET, if (container.width > SMALL_WIDTH) PAGE_HEADER_HEIGHT else PAGE_HEADER_HEIGHT_COMPACT)
                table.bounds  = Rectangle(INSET, header.height, header.width, container.height - header.height)

                button.bounds = when {
                    container.width > MEDIUM_WIDTH -> {
                        val size = Size(186, 45)
                        Rectangle(container.width - size.width - 20, (PAGE_HEADER_HEIGHT - size.height) / 2, size.width, size.height)
                    }
                    else           -> {
                        val size = Size(68, 68)
                        Rectangle(container.width - size.width - 20, container.height - size.height - 40, size.width, size.height)
                    }
                }
            }
        }
    }

    override fun shutdown() { /* no-op */ }
}

private const val EDIT_ICON_PATH   = "M0 18h3.75L14.81 6.94l-3.75-3.75L0 14.25V18zm2-2.92 9.06-9.06.92.92L2.92 16H2v-.92zM15.37.29a.996.996 0 00-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83a.996.996 0 000-1.41l-2.34-2.34z"
private const val DELETE_ICON_PATH = "M1 16c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V4H1v12zM14 1h-3.5l-1-1h-5l-1 1H0v2h14V1z"

private const val MEDIUM_WIDTH               = 768.0 // Moves create button down below this width
private const val SMALL_WIDTH                = 640.0 // Moves filter down below this width
private const val PAGE_HEADER_HEIGHT         =  64.0
private const val PAGE_HEADER_HEIGHT_COMPACT = 108.0 // Height of header for small viewport
        const val INSET                      =  16.0
