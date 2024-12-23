package io.nacular.doodle.examples.contacts

import io.nacular.doodle.animation.Animation
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.invoke
import io.nacular.doodle.controls.Photo
import io.nacular.doodle.controls.icons.PathIcon
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lerp
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
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.doodle.system.Cursor.Companion.Text
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.observable
import kotlin.math.max

/**
 * Renders the header portion of the main view.
 *
 * @param assets containing fonts, colors, etc.
 * @param animate the filter box
 * @param contacts model
 * @param navigator for going to contact list view
 * @param pathMetrics for measuring Paths
 * @param textMetrics for measuring text
 * @param focusManager to request focus
 */
class Header(
    private val assets      : AppConfig,
    private val animate     : Animator,
    private val contacts    : ContactsModel,
    private val navigator   : Navigator,
    private val pathMetrics : PathMetrics,
    private val textMetrics : TextMetrics,
    private val focusManager: FocusManager,
): View() {

    /**
     * Search box that filters which contacts are shown
     */
    private inner class FilterBox: View() {

        private var progress                     by renderProperty(0f)
        private var animation: Animation<Float>? by autoCanceling (  )

        private val searchIcon     = PathIcon<View>(path(assets.searchIcon)!!, fill = assets.search, pathMetrics = pathMetrics)
        private val searchIconSize = searchIcon.size(this)

        val minWidth = searchIconSize.width + 40.0

        val textField = TextField().apply {
            placeHolder      = "Search"
            borderVisible    = false
            backgroundColor  = Transparent
            placeHolderColor = assets.placeHolder
            focusChanged    += { _,_,hasFocus ->
                animation = animate(progress to if (hasFocus) 1f else 0f, using = assets.slowTransition) {
                    progress = it
                }
            }
        }

        init {
            cursor             = Text
            clipCanvasToBounds = false

            val clearButton = PathIconButton(pathData = assets.deleteIcon, pathMetrics = pathMetrics).apply {
                suggestHeight(44.0)

                cursor          = Pointer
                visible         = textField.text.isNotBlank()
                foregroundColor = assets.search
                fired += {
                    textField.text = ""
                }
            }

            textField.textChanged += { _,_,new ->
                when {
                    new.isBlank() -> contacts.filter = null
                    else          -> contacts.filter = { it.name.contains(new, ignoreCase = true) }
                }

                clearButton.visible = new.isNotBlank()
            }

            children += textField
            children += clearButton

            layout = constrain(children[0], children[1]) { textField, clear ->
                textField.left    eq searchIconSize.width + 2 * 20
                textField.right   eq clear.left
                textField.height  eq parent.height
                textField.centerY eq parent.centerY
                clear.width       eq 22
                (clear.right      eq parent.right - 20) strength Strong
                clear.centerY     eq textField.centerY
            }

            pointerChanged += clicked {
                focusManager.requestFocus(textField)
            }
        }

        override fun render(canvas: Canvas) {
            when {
                progress > 0f -> canvas.outerShadow(horizontal = 0.0, vertical = 4.0 * progress, color = assets.shadow, blurRadius = 3.0 * progress) {
                    // interpolate color during animation
                    canvas.rect(bounds.atOrigin, radius = 8.0, color = lerp(assets.searchSelected, assets.background, progress))
                }
                else          -> canvas.rect(bounds.atOrigin, radius = 8.0, color = assets.searchSelected)
            }

            searchIcon.render(this, canvas, at = Point(20.0, (height - searchIconSize.height) / 2))
        }
    }

            val naturalHeight          =  64.0
    private val filterRightAboveWidth  = 672.0
    private val filterCenterAboveWidth = 800.0
    private val filterBox: FilterBox

    val filterCentered: Boolean get() = width > filterCenterAboveWidth

    var searchEnabled by observable(true) { _,new ->
        filterBox.enabled = new
    }

    init {
        children += Photo(assets.logo).apply { suggestSize(Size(40)) }
        children += Label("Phonebook").apply {
            font            = assets.large
            behavior        = CommonLabelBehavior(textMetrics)
            acceptsThemes   = false
            foregroundColor = assets.header
        }
        children += FilterBox().apply { suggestSize(Size(300, 45)); font = assets.medium }.also { filterBox = it }

        val filterNaturalWidth = 300.0

        layout = simpleLayout { items, min, current, max, insets ->
            val (logo, label, filter) = items.toList()

            logo.updatePosition(2 * INSET, (naturalHeight - logo.bounds.height) / 2)
            label.updatePosition(logo.bounds.right + 10, logo.bounds.center.y - label.bounds.height / 2)

            filter.updateBounds(when {
                current.width > filterCenterAboveWidth -> Rectangle((current.width - filterNaturalWidth) / 2,        logo.bounds.center.y - filter.bounds.height / 2, filterNaturalWidth, filter.bounds.height)
                current.width > filterRightAboveWidth  -> Rectangle( current.width - filterNaturalWidth - 2 * INSET, logo.bounds.center.y - filter.bounds.height / 2, filterNaturalWidth, filter.bounds.height)
                else                                   -> Rectangle(logo.bounds.x, logo.bounds.bottom + INSET, max(filterBox.minWidth, current.width - 4 * INSET), filter.bounds.height)
            })

            Size(width, filterBox.bounds.bottom + 8)
        }

        // Custom cursor when pointer in the "clickable" region
        pointerMotionChanged += moved {
            cursor = when {
                it.inHotspot -> Pointer
                else         -> null
            }
        }

        // Show Contact list when "clickable" region clicked
        pointerChanged += clicked {
            if (it.inHotspot) {
                navigator.showContactList()
            }
        }
    }

    private val PointerEvent.inHotspot get() = this@Header.toLocal(location, target).let {
        val offset    = 10
        val filterBox = children[2]

        when {
            width <= filterRightAboveWidth -> it.y < filterBox.y - offset
            else                           -> it.x < filterBox.x - offset
        }
    }
}