package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.carousel.Carousel
import io.nacular.doodle.controls.theme.CommonButtonBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.inscribed
import io.nacular.doodle.geometry.inset
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.measured.units.Angle
import io.nacular.measured.units.times
import kotlin.math.min

class ButtonControls(carousel: Carousel<*, *>, textMetrics: TextMetrics, private val fonts: Fonts): View() {

    private val stroke = Stroke(White opacity 0.75f, 0.5)

    // Let pointer pass through everywhere else
    override fun contains(point: Point) = (point - position).let { localPoint -> children.any { it.contains(localPoint) } }

    init {
        //sampleStart
        children += leftButton  { carousel.previous() } // Skip to the previous frame
        children += rightButton { carousel.next    () } // Skip to the next frame

        // Progress Bar
        children += view {
            contains = { false }
            render   = {
                val progress = (carousel.nearestItem + carousel.progressToNextItem) / carousel.numItems

                rect(bounds.atOrigin,                     fill = stroke.fill           )
                rect(Rectangle(progress * width, height), fill = Color(0xE8B950u).paint)
            }

            carousel.progressChanged += { rerender() }
        }

        // Frame Number
        children += view {
            font     = fonts.mediumBoldFont
            contains = { false }
            render   = {
                val currentIndex = "0${(carousel.nearestItem    ) % carousel.numItems + 1}"
                val nextIndex    = "0${(carousel.nearestItem + 1) % carousel.numItems + 1}"
                val size1        = textMetrics.size(currentIndex, font)
                val size2        = textMetrics.size(nextIndex,    font)

                val x1 = (width  - size1.width ) / 2 - carousel.progressToNextItem * width
                val y1 = (height - size1.height) / 2
                val x2 = (width  - size2.width ) / 2 - carousel.progressToNextItem * width + width
                val y2 = (height - size2.height) / 2

                text(currentIndex, at = Point(x1, y1), font = font, fill = White.paint)
                text(nextIndex,    at = Point(x2, y2), font = font, fill = White.paint)
            }

            carousel.progressChanged += { rerender() }
        }
//sampleEnd

        layout = constrain(children[0], children[1], children[2], children[3]) { left, right, progress, text ->
            left.left    eq 0
            left.width   eq left.height
            left.height  eq 45
            left.centerY eq parent.centerY

            right.left    eq left.right + 10
            right.width   eq left.width
            right.height  eq left.height
            right.centerY eq left.centerY

            progress.left    eq right.right   + 20
            progress.right   eq text.left - 20
            progress.height  eq 1.5
            progress.centerY eq right.centerY

            text.top    eq 0
            text.width  eq 40
            text.right  eq parent.right - 20
            text.height eq parent.height
        }
    }

    private fun leftButton (onFired: (Button) -> Unit) = skipButton(isRightButton = false, onFired)
    private fun rightButton(onFired: (Button) -> Unit) = skipButton(isRightButton = true,  onFired)

    private fun skipButton(isRightButton: Boolean, onFired: (Button) -> Unit) = PushButton().apply {
        font          = fonts.smallRegularFont
        fired        += onFired
        cursor        = Pointer
        behavior      = buttonRenderer(right = isRightButton)
        acceptsThemes = false

        return this
    }

    private fun buttonRenderer(right: Boolean = true) = object: CommonButtonBehavior<Button>(null) {
        override fun contains(view: Button, point: Point): Boolean =
            point in Circle(view.bounds.center, min(view.width, view.height) / 2)

        override fun render(view: Button, canvas: Canvas) {
            val circle   = Circle(view.bounds.atOrigin.center, min(view.width, view.height) / 2)
            val triangle = AffineTransform.Identity.scale(around = circle.center, x = 0.7)(circle.inset(circle.radius * 0.75).inscribed(
                3,
                if (right) 90 * Angle.degrees else 270 * Angle.degrees
            )!!)

            val points = listOf(triangle.points[2], triangle.points[0], triangle.points[1])

            canvas.circle(circle.inset(stroke.thickness / 2), stroke = stroke)
            canvas.path  (points, Stroke(stroke.fill, 4 * stroke.thickness))
        }
    }
}
