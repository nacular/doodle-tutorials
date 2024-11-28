package io.nacular.doodle.examples

import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import org.kodein.di.instance

/**
 * Creates a [CalculatorApp]
 */
//sampleStart
fun main() {
    application(modules = listOf(FontModule, PointerModule)) {
        // load app
        CalculatorApp(
            display         = instance(),
            textMetrics     = instance(),
            fonts           = instance(),
            numberFormatter = NumberFormatterImpl()
        )
    }
}
//sampleEnd