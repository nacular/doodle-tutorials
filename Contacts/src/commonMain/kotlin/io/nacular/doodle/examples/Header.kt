package io.nacular.doodle.examples

import io.nacular.doodle.controls.Photo
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener.Companion.clicked
import io.nacular.doodle.event.PointerMotionListener.Companion.moved
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.path
import io.nacular.doodle.image.Image
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.doodle.system.Cursor.Companion.Text
import kotlin.math.max

/**
 * Renders the header portion of the main page
 */
class Header(
    private val fonts                 : AppFonts,
    private val navigator             : Navigator,
    private val textMetrics           : TextMetrics,
    private val pathMetrics           : PathMetrics,
    private val focusManager          : FocusManager,
    private val contactsModel         : ContactsModel,
                logoImage             : Image,
                filterCenterAboveWidth: Double,
                filterRightAboveWidth : Double,
                naturalHeight         : Double
): View() {

    // FIXME: Factor out hard coded colors

    /**
     * Search box that filters which contacts are shown
     */
    private inner class FilterBox: View() {
        val searchIconPath = path(SEARCH_ICON_PATH)
        val searchIconSize = pathMetrics.size(searchIconPath)

        private val textField = TextField().apply {
            font             = fonts.medium
            placeHolder      = "Search"
            borderVisible    = false
            backgroundColor  = Transparent
            placeHolderColor = PLACE_HOLDER_COLOR
            focusChanged    += { _,_,_ ->
                this@FilterBox.rerender()
            }
        }

        init {
            cursor             = Text
            clipCanvasToBounds = false

            val clearButton = PathIconButton(pathData = DELETE_ICON_PATH, pathMetrics = pathMetrics).apply {
                size            = Size(22, 44)
                visible         = textField.text.isNotBlank()
                foregroundColor = SEARCH_ICON_COLOR
                fired += {
                    textField.text = ""
                }
            }

            textField.textChanged += { _,_,new ->
                when {
                    new.isBlank() -> contactsModel.filter = null
                    else          -> contactsModel.filter = { it.name.contains(new, ignoreCase = true) }
                }

                clearButton.visible = new.isNotBlank()
            }

            children += textField
            children += clearButton

            layout = constrain(children[0], children[1]) { textField, clear ->
                textField.left    = parent.left + searchIconSize.width + 2 * 20
                textField.height  = parent.height
                textField.right   = clear.left
                textField.centerY = parent.centerY
                clear.right       = parent.right - 20
                clear.centerY     = parent.centerY
            }

            pointerChanged += clicked {
                focusManager.requestFocus(textField)
            }
        }

        override fun render(canvas: Canvas) {
            when {
                textField.hasFocus -> canvas.outerShadow(horizontal = 0.0, vertical = 0.0, color = Black.opacity(0.1f), blurRadius = 3.0) {
                    canvas.rect(bounds.atOrigin, radius = 8.0, color = White)
                }
                else               -> canvas.rect(bounds.atOrigin, radius = 8.0, color = Color(241u, 243u, 244u))
            }

            canvas.transform(AffineTransform.Identity.translate(20.0, (height - searchIconSize.height) / 2)) {
                canvas.path(searchIconPath, fill = SEARCH_ICON_COLOR.paint)
            }
        }
    }

    init {
        children += Photo(logoImage  ).apply { size = Size(40) }
        children += Label("Phonebook").apply {
            font = fonts.large
            foregroundColor = Color(95u, 99u, 104u)
            acceptsThemes = false
            behavior = CommonLabelBehavior(textMetrics)
        }
        children += FilterBox().apply { size = Size(300, 45) }

        val filterNaturalWidth = 300.0

        layout = Layout.simpleLayout { container ->
            val logo   = container.children[0]
            val label  = container.children[1]
            val filter = container.children[2]

            logo.position  = Point(2 * INSET, (naturalHeight - logo.height) / 2)
            label.position = Point(logo.bounds.right + 10, logo.bounds.center.y - label.height / 2)

            filter.bounds = when {
                container.width > filterCenterAboveWidth -> Rectangle((container.width - filterNaturalWidth) / 2,    logo.bounds.center.y - filter.height / 2, filterNaturalWidth, filter.height)
                container.width > filterRightAboveWidth  -> Rectangle( container.width - filterNaturalWidth - 2 * INSET, logo.bounds.center.y - filter.height / 2, filterNaturalWidth, filter.height)
                else                                     -> Rectangle(logo.x, container.height - filter.height - 8, max(0.0, container.width - 4 * INSET), filter.height)
            }
        }

        pointerMotionChanged += moved {
            cursor = when {
                it.inHotspot -> Pointer
                else         -> null
            }
        }

        pointerChanged += clicked {
            if (it.inHotspot) {
                navigator.showContactList()
            }
        }
    }

    private val PointerEvent.inHotspot get() = this@Header.toLocal(location, target).x < 220
}

private       val SEARCH_ICON_COLOR = Color(0x5f6368u)
private /*const*/ val SEARCH_ICON_PATH  = if (DESKTOP_WORK_AROUND) "M14 1.41 12.59 0 7 5.59 1.41 0 0 1.41 5.59 7 0 12.59 1.41 14 7 8.41 12.59 14 14 12.59 8.41 7Z" else "M12.5 11H11.71L11.43 10.73A6.471 6.471 0 0013 6.5 6.5 6.5 0 106.5 13C8.11 13 9.59 12.41 10.73 11.43L11 11.71V12.5L16 17.49 17.49 16 12.5 11ZM6.5 11C4.01 11 2 8.99 2 6.5S4.01 2 6.5 2 11 4.01 11 6.5 8.99 11 6.5 11Z"
private const val DELETE_ICON_PATH  = "M14 1.41 12.59 0 7 5.59 1.41 0 0 1.41 5.59 7 0 12.59 1.41 14 7 8.41 12.59 14 14 12.59 8.41 7Z"