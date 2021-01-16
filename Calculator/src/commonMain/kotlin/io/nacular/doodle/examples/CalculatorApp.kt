package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain

/**
 * Simple calculate app that places a [Calculator] at the center of the display.
 */
class CalculatorApp(
        display        : Display,
        textMetrics    : TextMetrics,
        fontDetector   : FontLoader,
        numberFormatter: NumberFormatter
): Application {
    init {
        // creat and display a single Calculator
        display += Calculator(fontDetector, textMetrics, numberFormatter).apply {
            // layout the Display whenever the Calculator's size preferences are updated.
            // this allows us to constrain its size to match its ideal size (which it sets).
            sizePreferencesChanged += { _,_,_ ->
                display.relayout()
            }
        }

        display.layout = constrain(display.children[0]) {
            it.width  = it.idealWidth  or constant(0.0) // set width to ideal width or 0 if no ideal size set
            it.height = it.idealHeight or constant(0.0) // set height to ideal height or 0 if no ideal size set
            it.center = parent.center
        }
    }

    override fun shutdown() { /* no-op */ }
}