package io.nacular.doodle.examples

import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.instance

/**
 * Creates a [CalculatorApp]
 */
//sampleStart
fun main() {
    application(modules = listOf(FontModule, PointerModule)) {
        // load app
        CalculatorApp(
            fonts           = instance(),
            display         = instance(),
            appScope        = CoroutineScope(SupervisorJob() + Dispatchers.UI),
            textMetrics     = instance(),
            numberFormatter = NumberFormatterImpl()
        )
    }
}
//sampleEnd