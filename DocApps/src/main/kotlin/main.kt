import io.nacular.doodle.HTMLElement
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.impl.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.DragDropModule
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.CalculatorApp
import io.nacular.doodle.examples.DataStore
import io.nacular.doodle.examples.FilterButtonProvider
import io.nacular.doodle.examples.LocalStorePersistence
import io.nacular.doodle.examples.NumberFormatterImpl
import io.nacular.doodle.examples.PersistentStore
import io.nacular.doodle.examples.PhotosApp
import io.nacular.doodle.examples.Router
import io.nacular.doodle.examples.TodoApp
import io.nacular.doodle.examples.TrivialRouter
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicCircularProgressIndicatorBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicMutableSpinnerBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

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

    application(root = element, modules = listOf(FontModule, ImageModule, PointerModule, KeyboardModule, basicLabelBehavior(),
            nativeTextFieldBehavior(), nativeHyperLinkBehavior(), nativeScrollPanelBehavior(smoothScrolling = true),
            Module(name = "AppModule") {
                bindSingleton<PersistentStore>      { LocalStorePersistence       (                      ) }
                bindSingleton                       { DataStore                   (instance()            ) }
                bindSingleton<Router>               { TrivialRouter               (window                ) }
                bindSingleton<FilterButtonProvider> { EmbeddedFilterButtonProvider(instance()            ) }

            }
    )) {
        // load app
        TodoApp(instance(), Dispatchers.UI, instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}

@JsName("photos")
fun photos(element: HTMLElement) {
    application(root = element, modules = listOf(
            FocusModule,
            ImageModule,
            KeyboardModule,
            DragDropModule,
            basicLabelBehavior(),
            nativeTextFieldBehavior(spellCheck = false),
            basicMutableSpinnerBehavior(),
            basicCircularProgressIndicatorBehavior(thickness = 18.0),
            Module(name = "AppModule") {
                bindSingleton<Animator> { AnimatorImpl   (instance(), instance()) }
            }
    )) {
        // load app
        PhotosApp(instance(), instance(), instance(), instance(), instance(), instance())
    }
}