package io.nacular.doodle.examples.contacts

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Simple contacts app based on https://phonebook-pi.vercel.app/
 *
 * @param theme for the app
 * @param Header factory for creating the [Header] view
 * @param router for managing app routes
 * @param assets creation for the app
 * @param display where all content for the app is shown
 * @param contacts data model
 * @param appScope for launching coroutines
 * @param navigator to show different views within the app
 * @param ContactView factory
 * @param ContactList factory
 * @param uiDispatcher allows dispatching to the UI thread
 * @param themeManager for setting the app's theme
 * @param CreateButton factory to obtain the app's create button
 * @param EditContactView factory to create the Contact edit view
 * @param CreateContactView factory to create the Contact creation view
 */
class ContactsApp(
    theme            : DynamicTheme,
    Header           : (AppConfig) -> Header,
    router           : Router,
    assets           : suspend () -> AppConfig,
    display          : Display,
    contacts         : ContactsModel,
    appScope         : CoroutineScope,
    navigator        : Navigator,
    ContactView      : (AppConfig, Contact) -> ContactView,
    ContactList      : (AppConfig         ) -> ContactList,
    uiDispatcher     : CoroutineDispatcher,
    themeManager     : ThemeManager,
    CreateButton     : (AppConfig         ) -> CreateContactButton,
    EditContactView  : (AppConfig, Contact) -> EditContactView,
    CreateContactView: (AppConfig         ) -> CreateContactView,
): Application {

    private lateinit var header     : Header
    private lateinit var contactList: View

    init {
        // Coroutine used to load assets
        appScope.launch(uiDispatcher) {
            val appAssets = assets()

            themeManager.selected = theme // Install theme

            header      = Header     (appAssets)
            contactList = ContactList(appAssets)

            // Register handlers for different routes
            router[""                 ] = { _,_        -> setMainView(display, contactList                              ) }
            router["/add"             ] = { _,_        -> setMainView(display, scrollPanel(CreateContactView(appAssets))) }
            router["/contact/([0-9]+)"] = { _, matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { contacts.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(display, scrollPanel(ContactView(appAssets, contact)))
                }
            }
            router["/contact/([0-9]+)/edit"] = { _, matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { contacts.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(display, scrollPanel(EditContactView(appAssets, contact)))
                }
            }

            display += header

            // Happens after header is added to ensure view goes below create button
            router.fireAction()

            display += CreateButton(appAssets)

            // Setup layout that manages how Header, CreateButton, and current View are positioned
            display.layout = object: Layout {
                // Header needs to be sized based on its minimumSize, so this layout should respond to any changes to it.
                override fun requiresLayout(
                    child: Positionable,
                    of   : PositionableContainer,
                    old  : View.SizePreferences,
                    new  : View.SizePreferences
                ) = new.minimumSize != old.minimumSize

                override fun layout(container: PositionableContainer) {
                    val mainView = container.children[1]
                    val button   = container.children[2]

                    header.size     = Size(container.width, header.minimumSize.height)
                    mainView.bounds = Rectangle(INSET, header.height, max(0.0, header.width - 2 * INSET), max(0.0, container.height - header.height))

                    button.bounds = when {
                        header.filterCentered -> Rectangle(container.width - appAssets.createButtonLargeSize.width - 20, (header.naturalHeight - appAssets.createButtonLargeSize.height) / 2, appAssets.createButtonLargeSize.width, appAssets.createButtonLargeSize.height)
                        else                  -> Rectangle(container.width - appAssets.createButtonSmallSize.width - 20, container.height - appAssets.createButtonSmallSize.height - 40,      appAssets.createButtonSmallSize.width, appAssets.createButtonSmallSize.height)
                    }
                }
            }

            display.fill(appAssets.background.paint)
        }
    }

    private fun scrollPanel(content: View) = ScrollPanel(content).apply {
        contentWidthConstraints = { parent.width }
    }

    private fun setMainView(display: Display, view: View) {
        when {
            display.children.size < 3 -> display             += view
            else                      -> display.children[1]  = view
        }

        header.searchEnabled = view == contactList
    }

    override fun shutdown() { /* no-op */ }
}