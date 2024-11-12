import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.DataStore
import io.nacular.doodle.examples.FilterButtonProvider
import io.nacular.doodle.examples.LinkFilterButtonProvider
import io.nacular.doodle.examples.PersistentStore
import io.nacular.doodle.examples.Router
import io.nacular.doodle.examples.TodoApp
import io.nacular.doodle.examples.io.nacular.doodle.examples.LocalStorePersistence
import io.nacular.doodle.examples.io.nacular.doodle.examples.TrivialRouter
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