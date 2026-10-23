package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.ideal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Simple calculate app that places a [Calculator] at the center of the display.
 */
//sampleStart
class CalculatorApp(
    display        : Display,
    appScope       : CoroutineScope,
    fonts          : FontLoader,
    textMetrics    : TextMetrics,
    numberFormatter: NumberFormatter
): Application {
    init {
        display += Calculator(fonts, appScope, textMetrics, numberFormatter)

        display.layout = constrain(display.first()) {
            it.top  eq      parent.centerY - readonly(it.height) / 2
            it.left eq      parent.centerX - readonly(it.width ) / 2
            it.size prefers ideal
        }
    }

    override fun shutdown() { /* no-op */ }
}
//sampleEnd