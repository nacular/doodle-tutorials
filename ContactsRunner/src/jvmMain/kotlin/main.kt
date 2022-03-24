package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.impl.AnimatorImpl
import io.nacular.doodle.application.Modules
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun main() {
    DESKTOP_WORK_AROUND = true
    val model = SimpleContactsModel()

    // Test Data
    model += Contact("Joe", "1234567")
    model += Contact("Jack", "1234567")
    model += Contact("Bob", "1234567")
    model += Contact("Jen", "1234567")
    model += Contact("Herman", "1234567")
    model += Contact("Lisa Fuentes", "1234567")
    model += Contact("Langston Hughes", "1234567")

    // Contacts App
    application (modules = listOf(
        FontModule,
        FocusModule,
        PointerModule,
        KeyboardModule,
        Modules.ImageModule,
        basicLabelBehavior       (),
        nativeTextFieldBehavior  (spellCheck = false),
        nativeHyperLinkBehavior  (),
        nativeScrollPanelBehavior(),
        Module(name = "AppModule") {
            bindInstance<ContactsModel>  { model }
            bindInstance<Router>         { TrivialRouter() }
            bindSingleton<Animator>      { AnimatorImpl         (instance(), instance()) }
            bindSingleton<PathMetrics>   { PathMetricsImpl      (instance()            ) }
            bindSingleton<ModalFactory>  { ModalFactoryImpl     (instance(), instance()) }
            bindSingleton<Navigator>     { NavigatorImpl        (instance(), model) }
            bindSingleton<ButtonFactory> { ButtonFactoryImpl(instance(), instance(), instance()) }
            bindSingleton<Modals>        { ModalsImpl(instance(), instance()) }
        }
    )) {
        // load app
        ContactsApp(
            fonts           = instance(),
            model           = model,
            theme           = instance(),
            images          = instance(),
            router          = instance(),
            modals          = instance(),
            display         = instance(),
            navigator       = instance(),
            pathMetrics     = instance(),
            textMetrics     = instance(),
            linkStyler      = instance(),
            uiDispatcher    = Dispatchers.UI,
            themeManager    = instance(),
            buttonFactory   = instance(),
            textFieldStyler = instance()
        )
    }
}