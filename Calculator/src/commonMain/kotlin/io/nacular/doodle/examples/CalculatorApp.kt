package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.layout.constraints.constrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Simple calculate app that places a [Calculator] at the center of the display.
 */
//sampleStart
class CalculatorApp(
    display        : Display,
    textMetrics    : TextMetrics,
    fonts          : FontLoader,
    numberFormatter: NumberFormatter
): Application {
    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        display += Calculator(fonts, appScope, textMetrics, numberFormatter)

        display.layout = constrain(display.first()) {
            it.size   eq it.idealSize
            it.center eq parent.center
        }
    }

    override fun shutdown() { /* no-op */ }
}
//sampleEnd