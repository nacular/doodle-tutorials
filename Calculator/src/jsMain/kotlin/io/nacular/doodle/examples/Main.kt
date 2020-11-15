package io.nacular.doodle.examples

import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.drawing.FontDetector
import io.nacular.doodle.drawing.impl.FontDetectorImpl
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

/**
 * Creates a [CalculatorApp]
 */
fun main() {
    application(modules = listOf(
        PointerModule,
        Module(name = "AppModule") {
            // Used to get fonts that should've been loaded
            bind<FontDetector>() with singleton { FontDetectorImpl(instance(), instance(), instance()) }
        }
    )) {
        // load app
        CalculatorApp(instance(), instance(), instance(), NumberFormatterImpl())
    }
}