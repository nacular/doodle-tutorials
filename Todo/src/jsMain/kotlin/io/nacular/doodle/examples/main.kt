package io.nacular.doodle.examples

import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Creates a [TodoApp]
 */
//sampleStart
fun main() {
    application(modules = listOf(
        FontModule,
        PointerModule,
        KeyboardModule,
        ImageModule,
        basicLabelBehavior(),
        nativeTextFieldBehavior(),
        nativeHyperLinkBehavior(),
        nativeScrollPanelBehavior(smoothScrolling = true),
        Module(name = "AppModule") {
            bindSingleton<PersistentStore> { LocalStorePersistence() }
            bindSingleton { DataStore(instance()) }
            bindSingleton<Router> { TrivialRouter(window) }
            bindSingleton<FilterButtonProvider> { LinkFilterButtonProvider(instance(), instance(), instance()) }
        }
    )) {
        // load app
        TodoApp(
            display              = instance(),
            fonts                = instance(),
            theme                = instance(),
            themes               = instance(),
            images               = instance(),
            dataStore            = instance(),
            linkStyler           = instance(),
            textMetrics          = instance(),
            focusManager         = instance(),
            uiDispatcher         = Dispatchers.UI,
            filterButtonProvider = instance()
        )
    }
}
//sampleEnd