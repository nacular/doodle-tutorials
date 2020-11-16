package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.center
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point

/**
 * Simple behavior for buttons in [Calculator].
 */
class CalcButtonBehavior(textMetrics: TextMetrics): CommonTextButtonBehavior<Button>(textMetrics) {
    override fun render(view: Button, canvas: Canvas) {
        val fillColor = view.run {
            when {
                model.selected    || model.pressed && model.armed -> backgroundColor?.lighter(0.50f)
                model.pointerOver || model.pressed                -> backgroundColor?.lighter(0.25f)
                else                                              -> backgroundColor
            }
        }

        fillColor?.let { canvas.rect(view.bounds.atOrigin, radius = view.height / 2, color = it) }

        canvas.text(view.text, view.font, textPosition(view), color = view.foregroundColor ?: White)
    }

    /**
     * Provide custom hit detection to fit rounding
     */
    override fun contains(view: Button, point: Point): Boolean {
        val radius      = view.height / 2
        val leftCircle  = Circle(center = Point(view.x + radius,            view.center.y), radius = radius)
        val rightCircle = Circle(center = Point(view.bounds.right - radius, view.center.y), radius = radius)

        return when {
            point.x < radius              -> point in leftCircle
            point.x > view.width - radius -> point in rightCircle
            else                          -> point in view.bounds
        }
    }
}