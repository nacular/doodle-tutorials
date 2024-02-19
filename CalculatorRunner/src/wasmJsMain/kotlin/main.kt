import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.examples.CalculatorApp
import io.nacular.doodle.examples.NumberFormatterImpl
import org.kodein.di.instance

/**
 * Creates a [CalculatorApp]
 */
fun main() {
    application(modules = listOf(FontModule, PointerModule)) {
        // load app
        CalculatorApp(instance(), instance(), instance(), NumberFormatterImpl())
    }
}