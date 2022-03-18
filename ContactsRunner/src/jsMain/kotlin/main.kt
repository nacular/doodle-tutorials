import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.impl.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.Contact
import io.nacular.doodle.examples.ModalFactory
import io.nacular.doodle.examples.ModalFactoryImpl
import io.nacular.doodle.examples.ContactsApp
import io.nacular.doodle.examples.SimpleContactsModel
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun main() {
    // Phonebook App
    application (modules = listOf(
        FontModule,
        ImageModule,
        FocusModule,
        PointerModule,
        KeyboardModule,
        basicLabelBehavior       (),
        nativeTextFieldBehavior  (spellCheck = false),
        nativeScrollPanelBehavior(),
        Module(name = "AppModule") {
            bindSingleton<Animator>     { AnimatorImpl    (instance(), instance()) }
            bindSingleton<PathMetrics>  { PathMetricsImpl (instance()            ) }
            bindSingleton<ModalFactory> { ModalFactoryImpl(instance(), instance()) }
        }
    )) {
        val model = SimpleContactsModel()

        // Test Data
        model += Contact("Joe", "1234567")
        model += Contact("Jack", "1234567")
        model += Contact("Bob", "1234567")
        model += Contact("Jen", "1234567")
        model += Contact("Herman", "1234567")
        model += Contact("Lisa Fuentes", "1234567")
        model += Contact("Langston Hughes", "1234567")

        // load app
        ContactsApp(
            display      = instance(),
            fonts        = instance(),
            images       = instance(),
            focusManager = instance(),
            themeManager = instance(),
            theme        = instance(),
            modals       = instance(),
            uiDispatcher = Dispatchers.UI,
            pathMetrics  = instance(),
            textMetrics  = instance(),
            model        = model,
        )
    }
}