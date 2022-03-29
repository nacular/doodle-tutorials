package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout.Companion.simpleLayout
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

/**
 * Simple contacts app based on https://phonebook-pi.vercel.app/
 *
 * @param theme for the app
 * @param Header factory for creating the [Header] view
 * @param router for
 */
class ContactsApp(
    theme            : DynamicTheme,
    Header           : (AppAssets) -> Header,
    router           : Router,
    assets           : suspend () -> AppAssets,
    display          : Display,
    contacts         : ContactsModel,
    appScope         : CoroutineScope,
    navigator        : Navigator,
    ContactView      : (AppAssets, Contact) -> ContactView,
    ContactList      : (AppAssets         ) -> ContactList,
    uiDispatcher     : CoroutineDispatcher,
    themeManager     : ThemeManager,
    CreateButton     : (AppAssets         ) -> CreateContactButton,
    EditContactView  : (AppAssets, Contact) -> EditContactView,
    CreateContactView: (AppAssets         ) -> CreateContactView,
): Application {

    init {
        appScope.launch(uiDispatcher) {
            val appAssets = assets()

            themeManager.selected = theme

            val header      = Header     (appAssets)
            val contactList = ContactList(appAssets)

            router[""                 ] = { _,_        -> setMainView(display, contactList                 ) }
            router["/add"             ] = { _,_        -> setMainView(display, CreateContactView(appAssets)) }
            router["/contact/([0-9]+)"] = { _, matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { contacts.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(display, ContactView(appAssets, contact))
                }
            }
            router["/contact/([0-9]+)/edit"] = { _, matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { contacts.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(display, EditContactView(appAssets, contact))
                }
            }

            display += header

            // Happens after header is added to ensure view goes below create button
            router.fireAction()

            display += CreateButton(appAssets)

            display.layout = simpleLayout { container ->
                val mainView = container.children[1]
                val button   = container.children[2]

                header.size     = Size(container.width, if (container.width > header.filterRightAboveWidth) header.naturalHeight else header.narrowHeight)
                mainView.bounds = Rectangle(INSET, header.height, header.width - 2 * INSET, container.height - header.height)

                button.bounds = when {
                    container.width > header.filterCenterAboveWidth -> {
                        val size = Size(186, 45)
                        Rectangle(container.width - size.width - 20, (header.naturalHeight - size.height) / 2, size.width, size.height)
                    }
                    else           -> {
                        val size = Size(68, 68)
                        Rectangle(container.width - size.width - 20, container.height - size.height - 40, size.width, size.height)
                    }
                }
            }

            display.fill(appAssets.background.paint)
        }
    }

    private fun setMainView(display: Display, view: View) {
        when {
            display.children.size < 3 -> display             += view
            else                      -> display.children[1]  = view
        }
    }

    override fun shutdown() { /* no-op */ }
}