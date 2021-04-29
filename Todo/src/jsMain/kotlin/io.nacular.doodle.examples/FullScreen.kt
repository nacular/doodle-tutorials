package io.nacular.doodle.examples

import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun fullscreen() {
    application(modules = listOf(FontModule, PointerModule, KeyboardModule, basicLabelBehavior(),
        nativeTextFieldBehavior(), nativeHyperLinkBehavior(), nativeScrollPanelBehavior(smoothScrolling = true),
        Module(name = "AppModule") {
            bindSingleton<ImageLoader>          { ImageLoaderImpl         (instance(), instance()            ) }
            bindSingleton<PersistentStore>      { LocalStorePersistence   (                                  ) }
            bindSingleton<NativeLinkStyler>     { NativeLinkStylerImpl    (instance()                        ) }
            bindSingleton                       { DataStore               (instance()                        ) }
            bindSingleton<Router>               { TrivialRouter           (window                            ) }
            bindSingleton<FilterButtonProvider> { LinkFilterButtonProvider(instance(), instance(), instance()) }
        }
    )) {
        // load app
        TodoApp(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}