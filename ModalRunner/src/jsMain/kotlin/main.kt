import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.impl.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.ModalApp
import io.nacular.doodle.examples.ModalFactory
import io.nacular.doodle.examples.ModalFactoryImpl
import io.nacular.doodle.examples.PhoneBookApp
import io.nacular.doodle.image.Image
import io.nacular.doodle.theme.basic.BasicTheme
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicButtonBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicTableBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun main() {
    // Modal app
    if (false) {
        application(modules = listOf(
            FocusModule,
            PointerModule,
            KeyboardModule,
            basicLabelBehavior     (),
            basicButtonBehavior    (),
            nativeTextFieldBehavior(spellCheck = false),
            Module(name = "AppModule") {
                bindSingleton<Animator>     { AnimatorImpl    (instance(), instance()) }
                bindSingleton<ModalFactory> { ModalFactoryImpl(instance(), instance()) }
            }
        )) {
            // load app
            ModalApp(instance(), instance(), instance(), instance(), instance(), instance())
        }
    }

    // Phonebook App
    application (modules = listOf(
        FocusModule,
        FontModule,
        PointerModule,
        KeyboardModule,
        ImageModule,
        basicTableBehavior       (),
        basicLabelBehavior       (),
        basicButtonBehavior      (),
        nativeTextFieldBehavior  (spellCheck = false),
        nativeScrollPanelBehavior(),
        Module(name = "AppModule") {
            bindSingleton<Animator>     { AnimatorImpl    (instance(), instance()) }
            bindSingleton<ModalFactory> { ModalFactoryImpl(instance(), instance()) }
        }
    )) {
        // load app
        PhoneBookApp(
            display      = instance(),
            fonts        = instance(),
            images       = instance(),
            focusManager = instance(),
            themeManager = instance(),
            theme        = instance(),
            Modal        = instance(),
            uiDispatcher = Dispatchers.UI,
            textMetrics  = instance()
        )
    }
}