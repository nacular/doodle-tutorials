package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.PathModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Creates a [TabStripApp]
 */
fun main() {
    application(modules = listOf(PointerModule, PathModule, Module(name = "AppModule") {
        bindSingleton<Animator> { AnimatorImpl(instance(), instance()) }
    })) {
        // load app
        TabStripApp(display = instance(), animator = instance(), pathMetrics = instance())
    }
}