package io.nacular.doodle.examples

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import kotlinx.serialization.json.Json
import org.kodein.di.instance

fun main() {
    application(modules = listOf(
        ImageModule,
        PointerModule,
        nativeScrollPanelBehavior(),
    )) {
        // load app
        PhotoStreamApp(
            display    = instance (), // always available
            themes      = instance(), // available since nativeScrollPanelBehavior loaded
            theme       = instance(), // available since nativeScrollPanelBehavior loaded
            httpClient  = HttpClient {
               install(ContentNegotiation) {
                   json(Json { ignoreUnknownKeys = true })
               }
            },
            imageLoader = instance() // available via "AppModule" above
        )
    }
}