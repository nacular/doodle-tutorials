package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 5/1/20.
 */
class CalcButtonBehavior(private val textMetrics: TextMetrics): CommonTextButtonBehavior<Button>(textMetrics) {
    override fun render(view: Button, canvas: Canvas) {
        val fillColor = view.run {
            when {
                model.selected    || model.pressed && model.armed -> backgroundColor?.lighter(0.50f)
                model.pointerOver || model.pressed                -> backgroundColor?.lighter(0.25f)
                else                                              -> backgroundColor
            }
        }

        val textPosition = textMetrics.size(view.text, view.font).run {
            Point((view.height - width) / 2, (view.height - height) / 2)
        }

        fillColor?.let { canvas.rect(view.bounds.atOrigin, radius = view.height / 2, color = it) }

        canvas.text(view.text, view.font, textPosition, color = view.foregroundColor ?: Color.White)
    }

    override fun contains(view: Button, point: Point) = point in view.bounds
}