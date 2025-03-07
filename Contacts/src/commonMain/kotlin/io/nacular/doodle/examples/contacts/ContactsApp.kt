@file:Suppress("LocalVariableName")

package io.nacular.doodle.examples.contacts

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Simple contacts app based on https://phonebook-pi.vercel.app/
 *
 * @param theme for the app
 * @param Header factory for creating the [Header] view
 * @param router for managing app routes
 * @param config creation for the app
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
    config           : suspend () -> AppConfig,
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

//sampleStart
    init {
        // Coroutine used to load config
        appScope.launch(uiDispatcher) {
            themeManager.selected = theme // Install theme

            val appConfig = config() // load app configuration

            header        = Header     (appConfig)
            contactList   = ContactList(appConfig)

            // Register handlers for different routes
            router[""                 ] = { _,_        -> setMainView(display, contactList                              ) }
            router["/add"             ] = { _,_        -> setMainView(display, scrollPanel(CreateContactView(appConfig))) }
            router["/contact/([0-9]+)"] = { _, matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { contacts.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(display, scrollPanel(ContactView(appConfig, contact)))
                }
            }
            router["/contact/([0-9]+)/edit"] = { _, matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { contacts.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(display, scrollPanel(EditContactView(appConfig, contact)))
                }
            }

            display += header

            // Happens after header is added to ensure view goes below create button
            router.fireAction()

            // Reset layout whenever display children change since the layout stores the display's children internally.
            display.childrenChanged += { _,_ ->
                updateLayout(display, appConfig)
            }

            display += CreateButton(appConfig)

            display.fill(appConfig.background.paint)
        }
    }
//sampleEnd

    private fun scrollPanel(content: View) = ScrollPanel(content).apply {
        contentWidthConstraints  = { it eq width - verticalScrollBarWidth }
        contentHeightConstraints = { it eq it.idealValue                  }
    }

    private fun setMainView(display: Display, view: View) {
        when {
            display.children.size < 3 -> display             += view
            else                      -> display.children[1]  = view
        }

        header.searchEnabled = view == contactList
    }

    private fun updateLayout(display: Display, appAssets: AppConfig) {
        display.layout = if (display.children.size < 3) null else constrain(header, display.children[1], display.children[2]) { header_, mainView, button ->
            header_.top     eq 0
            header_.width   eq parent.width
            header_.height  eq header_.idealHeight

            mainView.top    eq header_.height
            mainView.left   eq INSET
            mainView.width  eq header_.width - 2 * INSET
            mainView.bottom eq parent.bottom

            lateinit var buttonSize: Size

            when {
                header.filterCentered -> {
                    buttonSize = appAssets.createButtonLargeSize
                    button.top eq (header.naturalHeight - buttonSize.height) / 2
                }
                else                  -> {
                    buttonSize = appAssets.createButtonSmallSize
                    button.bottom eq parent.bottom - 40
                }
            }

            button.left  greaterEq 20
            button.right eq        parent.right - 20 strength Strong
            button.size  eq        buttonSize
        }
    }

    override fun shutdown() { /* no-op */ }
}