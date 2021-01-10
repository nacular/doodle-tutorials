package io.nacular.doodle.examples

import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.FontDetector
import io.nacular.doodle.drawing.impl.FontDetectorImpl
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicListBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.NativeTextFieldBehavior
import io.nacular.doodle.tutorials.TodoApp
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

/**
 * Creates a [TodoApp]
 */
fun main() {
    application(modules = listOf(
        PointerModule,
        KeyboardModule,
        basicLabelBehavior(foregroundColor = Color(0x4D4D4Du)),
        NativeTextFieldBehavior,
        Module(name = "AppModule") {
            bind<ImageLoader >() with singleton { ImageLoaderImpl (instance(), instance()            ) }
            bind<FontDetector>() with singleton { FontDetectorImpl(instance(), instance(), instance()) }
        }
    )) {
        // load app
        TodoApp(instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}