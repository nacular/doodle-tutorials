import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.DataStore
import io.nacular.doodle.examples.FilePersistence
import io.nacular.doodle.examples.FilterButtonProvider
import io.nacular.doodle.examples.PersistentStore
import io.nacular.doodle.examples.Router
import io.nacular.doodle.examples.TodoApp
import io.nacular.doodle.examples.InMemoryRouter
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Created by Nicholas Eddy on 6/8/21.
 */
fun main() {
    class EmbeddedFilterButtonProvider(private val dataStore: DataStore): FilterButtonProvider {
        override fun invoke(text: String, filter: DataStore.Filter?, behavior: Behavior<Button>) = PushButton(text).apply {
            this.behavior               = behavior
            this.acceptsThemes          = false
            fired += { dataStore.filter = filter }

            dataStore.filterChanged += { rerender() }
        }
    }

    application(modules = listOf(FontModule, PointerModule, KeyboardModule, ImageModule, basicLabelBehavior(),
            nativeTextFieldBehavior(), nativeHyperLinkBehavior(), nativeScrollPanelBehavior(),
            Module(name = "AppModule") {
                bindSingleton<PersistentStore>      { FilePersistence()                        }
                bindSingleton                       { DataStore(instance())                    }
                bindSingleton<Router>               { InMemoryRouter()                          }
                bindSingleton<FilterButtonProvider> { EmbeddedFilterButtonProvider(instance()) }
            }
    )) {
        // load app
        TodoApp(
            display              = instance(),
            uiDispatcher         = Dispatchers.UI,
            fonts                = instance(),
            theme                = instance(),
            themes               = instance(),
            images               = instance(),
            dataStore            = instance(),
            linkStyler           = instance(),
            textMetrics          = instance(),
            focusManager         = instance(),
            filterButtonProvider = instance()
        )
    }
}