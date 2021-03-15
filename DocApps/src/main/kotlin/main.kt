import io.nacular.doodle.HTMLElement
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.examples.CalculatorApp
import io.nacular.doodle.examples.DataStore
import io.nacular.doodle.examples.FilterButtonProvider
import io.nacular.doodle.examples.LocalStorePersistence
import io.nacular.doodle.examples.NativeLinkStyler
import io.nacular.doodle.examples.NativeLinkStylerImpl
import io.nacular.doodle.examples.NumberFormatterImpl
import io.nacular.doodle.examples.PersistentStore
import io.nacular.doodle.examples.Router
import io.nacular.doodle.examples.TodoApp
import io.nacular.doodle.examples.TrivialRouter
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

@JsName("calculator")
fun calculator(element: HTMLElement) {
    application(root = element, modules = listOf(FontModule, PointerModule)) {
        // load app
        CalculatorApp(instance(), instance(), instance(), NumberFormatterImpl())
    }
}

@JsName("todo")
fun todo(element: HTMLElement) {
    class EmbeddedFilterButtonProvider(private val dataStore: DataStore): FilterButtonProvider {
        override fun invoke(text: String, filter: DataStore.Filter?, behavior: Behavior<Button>) = PushButton(text).apply {
            this.behavior               = behavior
            this.acceptsThemes          = false
            fired += { dataStore.filter = filter }

            dataStore.filterChanged += { rerender() }
        }
    }

    application(root = element, modules = listOf(FontModule, PointerModule, KeyboardModule, basicLabelBehavior(),
            nativeTextFieldBehavior(), nativeHyperLinkBehavior(), nativeScrollPanelBehavior(smoothScrolling = true),
            Module(name = "AppModule") {
                bind<ImageLoader>         () with singleton { ImageLoaderImpl             (instance(), instance()) }
                bind<PersistentStore>     () with singleton { LocalStorePersistence       (                      ) }
                bind<NativeLinkStyler>    () with singleton { NativeLinkStylerImpl        (instance()            ) }
                bind<DataStore>           () with singleton { DataStore                   (instance()            ) }
                bind<Router>              () with singleton { TrivialRouter               (window                ) }
                bind<FilterButtonProvider>() with singleton { EmbeddedFilterButtonProvider(instance()            ) }

            }
    )) {
        // load app
        TodoApp(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}