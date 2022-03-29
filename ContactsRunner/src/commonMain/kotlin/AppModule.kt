import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.impl.AnimatorImpl
import io.nacular.doodle.examples.AppAssets
import io.nacular.doodle.examples.AppButtons
import io.nacular.doodle.examples.AppButtonsImpl
import io.nacular.doodle.examples.Contact
import io.nacular.doodle.examples.ContactList
import io.nacular.doodle.examples.ContactView
import io.nacular.doodle.examples.ContactsModel
import io.nacular.doodle.examples.CreateContactButton
import io.nacular.doodle.examples.CreateContactView
import io.nacular.doodle.examples.EditContactView
import io.nacular.doodle.examples.Header
import io.nacular.doodle.examples.ModalFactory
import io.nacular.doodle.examples.ModalFactoryImpl
import io.nacular.doodle.examples.Modals
import io.nacular.doodle.examples.ModalsImpl
import io.nacular.doodle.examples.Navigator
import io.nacular.doodle.examples.NavigatorImpl
import io.nacular.doodle.examples.SimpleContactsModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.DI.Module
import org.kodein.di.bindFactory
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun appModule(appScope: CoroutineScope, uiDispatcher: CoroutineDispatcher, contacts: SimpleContactsModel) = Module(name = "AppModule") {
    bindInstance<ContactsModel>  { contacts }
    bindSingleton<Animator>      { AnimatorImpl     (instance(), instance())             }
    bindSingleton<ModalFactory>  { ModalFactoryImpl (instance(), instance())             }
    bindSingleton<Navigator>     { NavigatorImpl    (instance(), contacts)               }
    bindSingleton<AppButtons>    { AppButtonsImpl   (instance(), instance(), instance()) }
    bindSingleton<Modals>        { ModalsImpl       (instance(), instance())             }

    bindFactory<AppAssets, ContactList> {
        ContactList(
            assets       = it,
            modals       = instance(),
            appScope     = appScope,
            contacts     = contacts,
            navigator    = instance(),
            textMetrics  = instance(),
            pathMetrics  = instance(),
            uiDispatcher = uiDispatcher
        )
    }

    bindFactory<AppAssets, CreateContactView> {
        CreateContactView(
            assets          = it,
            navigator       = instance(),
            buttons         = instance(),
            pathMetrics     = instance(),
            textMetrics     = instance(),
            textFieldStyler = instance(),
        )
    }

    bindFactory<Pair<AppAssets, Contact>, ContactView> { (assets, contact) ->
        ContactView(
            assets       = assets,
            modals       = instance(),
            buttons      = instance(),
            contact      = contact,
            appScope     = appScope,
            navigator    = instance(),
            linkStyler   = instance(),
            pathMetrics  = instance(),
            textMetrics  = instance(),
            uiDispatcher = uiDispatcher
        )
    }

    bindFactory<Pair<AppAssets, Contact>, EditContactView> { (assets, contact) ->
        EditContactView(
            assets          = assets,
            modals          = instance(),
            contact         = contact,
            buttons         = instance(),
            appScope        = appScope,
            navigator       = instance(),
            pathMetrics     = instance(),
            textMetrics     = instance(),
            uiDispatcher    = uiDispatcher,
            textFieldStyler = instance(),
        )
    }

    bindFactory<AppAssets, Header> {
        Header(
            assets       = it,
            animate      = instance(),
            contacts     = contacts,
            navigator    = instance(),
            textMetrics  = instance(),
            pathMetrics  = instance(),
            focusManager = instance(),
        )
    }

    bindFactory<AppAssets, CreateContactButton> {
        CreateContactButton(assets = it, router = instance(), textMetrics = instance(), animate = instance())
    }
}