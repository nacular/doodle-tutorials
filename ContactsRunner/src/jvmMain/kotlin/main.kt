import io.nacular.doodle.application.Modules
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.AppAssets
import io.nacular.doodle.examples.AppAssetsImpl
import io.nacular.doodle.examples.Contact
import io.nacular.doodle.examples.ContactView
import io.nacular.doodle.examples.ContactsApp
import io.nacular.doodle.examples.EditContactView
import io.nacular.doodle.examples.FilePersistence
import io.nacular.doodle.examples.Router
import io.nacular.doodle.examples.SimpleContactsModel
import io.nacular.doodle.examples.TrivialRouter
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI.Module
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.factory
import org.kodein.di.instance

fun main() {
    val contacts = SimpleContactsModel(FilePersistence())
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

        // Contacts App
    application (modules = listOf(
        FontModule,
        FocusModule,
        PointerModule,
        KeyboardModule,
        Modules.ImageModule,
        basicLabelBehavior       (),
        nativeTextFieldBehavior  (spellCheck = false),
        nativeHyperLinkBehavior  (),
        nativeScrollPanelBehavior(),
        appModule(appScope = appScope, contacts = contacts, uiDispatcher = Dispatchers.UI),
        Module   (name = "PlatformModule") {
            bindInstance<Router>       { TrivialRouter  (          ) }
            bindSingleton<PathMetrics> { PathMetricsImpl(instance()) }
        }
    )) {
        // load app
        ContactsApp(
            theme             = instance(),
            assets            = { val a = AppAssetsImpl(instance(), instance()); object: AppAssets by a {
                // FIXME: Remove when https://github.com/JetBrains/skiko/issues/518 (which causes app crash) is fixed
                val exclamation = "M3.402 0C4.752 0 5.454.675 5.427 1.998 5.4 4.05 5.4 5.4 4.725 10.125 4.59 11.07 4.32 11.475 3.375 11.475 3.375 11.475 2.025 11.475 2.025 11.475 1.08 11.475.81 11.07.675 10.125 0 5.535 0 4.05 0 2.052 0 .675.675 0 2.025 0M2.7 13.5C3.429 13.5 4.104 13.797 4.617 14.283 5.103 14.796 5.4 15.471 5.4 16.2 5.4 17.685 4.185 18.9 2.7 18.9 1.215 18.9 0 17.685 0 16.2 0 15.471.297 14.796.783 14.283 1.296 13.797 1.971 13.5 2.7 13.5 2.7 13.5 2.7 13.5 2.7 13.5"

                override val editIcon   = exclamation
                override val nameIcon   = exclamation
                override val phoneIcon  = exclamation
                override val searchIcon = exclamation
            } },
            router            = instance(),
            Header            = factory(),
            display           = instance(),
            contacts          = contacts,
            appScope          = appScope,
            navigator         = instance(),
            ContactList       = factory(),
            uiDispatcher      = Dispatchers.UI,
            ContactView       = { assets, contact -> factory<Pair<AppAssets, Contact>, ContactView>()(assets to contact) },
            CreateButton      = factory(),
            themeManager      = instance(),
            EditContactView   = { assets, contact -> factory<Pair<AppAssets, Contact>, EditContactView>()(assets to contact) },
            CreateContactView = factory(),
        )
    }
}