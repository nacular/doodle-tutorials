package io.nacular.doodle.examples

import io.ktor.client.HttpClient
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun main() {
    application(modules = listOf(
        PointerModule,
        nativeScrollPanelBehavior(),
        Module(name = "AppModule") {
            bindSingleton<ImageLoader> { ImageLoaderImpl(instance(), instance()) }
        }
    )) {
        // load app
        PhotoStreamApp(display     = instance  (), // always available
                       themes      = instance  (), // available since nativeScrollPanelBehavior loaded
                       theme       = instance  (), // available since nativeScrollPanelBehavior loaded
                       httpClient  = HttpClient(),
                       imageLoader = instance  ()) // available via "AppModule" above
    }
}