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
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

fun main() {
    application(modules = listOf(FontModule, PointerModule, KeyboardModule, basicLabelBehavior(),
        nativeTextFieldBehavior(), nativeHyperLinkBehavior(), nativeScrollPanelBehavior(smoothScrolling = true),
        Module(name = "AppModule") {
            bind<ImageLoader>     () with singleton { ImageLoaderImpl      (instance(), instance()) }
            bind<PersistentStore> () with singleton { LocalStorePersistence(                      ) }
            bind<NativeLinkStyler>() with singleton { NativeLinkStylerImpl (instance()            ) }
            bind<DataStore>       () with singleton { DataStore            (instance()            ) }
            bind<Router>          () with singleton { TrivialRouter        (window                ) }
        }
    )) {
        // load app
        TodoApp(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}