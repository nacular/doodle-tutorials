package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.ModalModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Creates a [TimedCardsApp]
 */
fun main() {
    application(modules = listOf(
        FontModule,
        ImageModule,
        ModalModule,
        PointerModule,
        KeyboardModule,
        basicLabelBehavior(White),
        Module(name = "App") {
            bindSingleton<Animator> { AnimatorImpl(instance(), instance()) }
        }
    )) {
        // load app
        TimedCardsApp(
            display      = instance(),
            focusManager = instance(),
            themeManager = instance(),
            theme        = instance(),
            images       = instance(),
            fonts        = instance(),
            animate      = instance(),
            textMetrics  = instance(),
            timer        = instance(),
            scheduler    = instance(),
            uiDispatcher = Dispatchers.UI
        )
    }
}