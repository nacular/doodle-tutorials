package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.layout.constraints.constrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // creat and display a single Calculator
        display += Calculator(fontDetector, appScope, textMetrics, numberFormatter).apply {
            // layout the Display whenever the Calculator's size preferences are updated.
            // this allows us to constrain its size to match its ideal size (which it sets).
            sizePreferencesChanged += { _,_,_ ->
                display.relayout()
            }
        }

        display.layout = constrain(display.children[0]) {
            it.width  eq (display.children[0].idealSize?.width  ?: 0.0) // set width to ideal width or 0 if no ideal size set
            it.height eq (display.children[0].idealSize?.height ?: 0.0) // set height to ideal height or 0 if no ideal size set
            it.center eq parent.center
        }
    }

    override fun shutdown() { /* no-op */ }
}