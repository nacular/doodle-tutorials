package io.nacular.doodle.examples.calculator

import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.examples.Calculator
import io.nacular.doodle.examples.NumberFormatter
import io.nacular.doodle.layout.constraints.constrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CalculatorImages(
    display        : Display,
    fonts          : FontLoader,
    textMetrics    : TextMetrics,
    numberFormatter: NumberFormatter
): Application {
    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val (calculator1, calculator2) = (0..1).map {
            Calculator(fonts, appScope, textMetrics, numberFormatter).apply {
                // layout the Display whenever the Calculator's size preferences are updated.
                // this allows us to constrain its size to match its ideal size (which it sets).
                sizePreferencesChanged += { _,_,_ ->
                    display.relayout()
                }
            }
        }

        display += calculator1
        display += calculator2

        display.layout = constrain(calculator1, calculator2) { c1, c2 ->
            c1.left    eq 0
            c1.width   eq (display.children.firstOrNull()?.idealSize?.width  ?: 0.0)
            c1.height  eq (display.children.firstOrNull()?.idealSize?.height ?: 0.0)
            c1.centerY eq parent.centerY

            c2.right   eq parent.right
            c2.width   eq (display.children.firstOrNull()?.idealSize?.width  ?: 0.0)
            c2.height  eq (display.children.firstOrNull()?.idealSize?.height ?: 0.0)
            c2.centerY eq parent.centerY
        }
    }

    override fun shutdown() {}
}