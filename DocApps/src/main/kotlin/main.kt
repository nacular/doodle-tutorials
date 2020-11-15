import io.nacular.doodle.HTMLElement
import io.nacular.doodle.application.Modules
import io.nacular.doodle.application.application
import io.nacular.doodle.drawing.FontDetector
import io.nacular.doodle.drawing.impl.FontDetectorImpl
import io.nacular.doodle.examples.CalculatorApp
import io.nacular.doodle.examples.NumberFormatterImpl
import org.kodein.di.Kodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

@JsName("calculator")
fun calculator(element: HTMLElement) {
    application(root = element, modules = listOf(
            Modules.PointerModule,
            Kodein.Module(name = "AppModule") {
                // Used to get fonts that should've been loaded
                bind<FontDetector>() with singleton { FontDetectorImpl(instance(), instance(), instance()) }
            }
    )) {
        // load app
        CalculatorApp(instance(), instance(), instance(), NumberFormatterImpl())
    }
}