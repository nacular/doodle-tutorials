package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.PathModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Creates a [AnimatingFormApp]
 */
fun main() {
    application(modules = listOf(
        PointerModule,
        PathModule,
        FontModule,
        basicLabelBehavior(),
        nativeTextFieldBehavior(),
        Module(name = "AppModule") {
            bindSingleton<Animator> { AnimatorImpl(instance(), instance()) }
        }
    )) {
        // load app
        AnimatingFormApp(
            fonts           = instance(),
            theme           = instance(),
            display         = instance(),
            animator        = instance(),
            textMetrics     = instance(),
            pathMetrics     = instance(),
            themeManager    = instance(),
            textFieldStyler = instance()
        )
    }
}