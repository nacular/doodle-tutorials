package io.nacular.doodle.examples.contacts

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Application
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.ModalModule
import io.nacular.doodle.application.Modules.Companion.PathModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.center
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.examples.contacts.ContactsModel.EditContext
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.BasicTheme
import io.nacular.doodle.theme.native.NativeTheme
import io.nacular.doodle.utils.Resizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.DI.Module
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import kotlin.Result.Companion.success
import kotlin.math.max
import kotlin.math.min

// Used during showcase to ignore any navigation
private object NoOpNavigator: Navigator {
    override fun showContact      (contact: Contact) { /* no-op */ }
    override fun showContactEdit  (contact: Contact) { /* no-op */ }
    override fun showContactList  (                ) { /* no-op */ }
    override fun showCreateContact(                ) { /* no-op */ }
    override fun goBack           (                ) { /* no-op */ }
}

// Used to avoid persisting any changes during showcase
object NoOpPersistence: PersistentStore<Contact> {
    override fun load() = emptyList<Contact>()
    override fun save(tasks: List<Contact>) { /* no-op */ }
}

// Model used in showcase that basically does nothing and treats all edits as successful
object NoOpContacts: ContactsModel {
    override var filter: ((Contact) -> Boolean)? = null
    override fun plusAssign (contact: Contact) {/* no-op */ }
    override fun minusAssign(contact: Contact) {/* no-op */ }
    override fun edit(existing: Contact, block: EditContext.() -> Unit): Result<Contact> = EditContext().apply(block).let {
        success(Contact(it.name ?: existing.name, it.phoneNumber ?: existing.phoneNumber))
    }

    override fun id  (of: Contact): Int?     = null
    override fun find(id: Int    ): Contact? = null
}

// Modules used in showcase apps
val showcaseModules = listOf(
    PathModule,
    FontModule,
    ImageModule,
    ModalModule,
    FocusModule,
    PointerModule,
    KeyboardModule,
    BasicTheme.basicLabelBehavior(),
    NativeTheme.nativeTextFieldBehavior(spellCheck = false),
    NativeTheme.nativeHyperLinkBehavior(),
    NativeTheme.nativeScrollPanelBehavior(),
    Module(name = "DummyModule") {
        bindInstance<Router>      { EmbeddedRouter(                                  ) }
        bindSingleton<Animator>   { AnimatorImpl  (instance(), instance()            ) }
        bindInstance <Navigator>  { NoOpNavigator                                      }
        bindSingleton<AppButtons> { AppButtonsImpl(instance(), instance(), instance()) }
        bindSingleton<Modals>     { ModalsImpl    (instance(), instance(), instance()) }
    }
)

/**
 * Allows hosting of single Views from the Contacts app within the documentation.
 */
fun showcase(
    theme       : DynamicTheme,
    assets      : suspend () -> AppConfig,
    display     : Display,
    appScope    : CoroutineScope,
    themeManager: ThemeManager,
    view        : (AppConfig) -> View,
): Application {
    return object: Application {
        init {
            appScope.launch(Dispatchers.UI) {
                themeManager.selected = theme

                val inset  = 20.0
                val config = assets()

                display += object: Container() {
                    init {
                        size               = Size(display.width - 2 * inset, display.height - 2 * inset)
                        position           = display.center - Point(width / 2, height / 2)
                        clipCanvasToBounds = false

                        Resizer(this).apply { movable = false }

                        this += view(config)

                        this.layout = constrain(children[0]) {
                            fill(insets = Insets(all = 8.0))(it)
                        }
                    }

                    override fun render(canvas: Canvas) {
                        canvas.outerShadow(vertical = 2.0, blurRadius = 50.0, color = Black.opacity(0.1f)) {
                            rect(bounds.atOrigin, color = White)
                        }
                    }
                }

                display.layout = Layout.simpleLayout {
                    it.children[0].apply {
                        val x = max(inset, x)
                        val y = max(inset, y)
                        bounds = Rectangle(
                            x,
                            y,
                            min(display.width  - inset - x, width),
                            min(display.height - inset - y, height)
                        )
                    }
                }
            }
        }

        override fun shutdown() { /* no-op */ }
    }
}