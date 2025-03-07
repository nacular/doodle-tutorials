package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.DragDropModule
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.application
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicCircularProgressIndicatorBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicMutableSpinButtonBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import kotlin.random.Random

/**
 * Creates a [PhotosApp]
 */
fun main() {
    application(modules = listOf(
        FocusModule,
        ImageModule,
        KeyboardModule,
        DragDropModule,
        basicLabelBehavior(),
        nativeTextFieldBehavior(spellCheck = false),
        basicMutableSpinButtonBehavior(),
        basicCircularProgressIndicatorBehavior(thickness = 18.0),
        Module(name = "AppModule") {
            bindSingleton<Animator> { AnimatorImpl(instance(), instance()) }
        }
    )) {
        // load app
        PhotosApp(
            theme        = instance(),
            images       = instance(),
            random       = Random,
            display      = instance(),
            animate      = instance(),
            themeManager = instance(),
            focusManager = instance()
        )
    }
}