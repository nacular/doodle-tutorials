package io.nacular.doodle.examples

import io.nacular.doodle.controls.MutableListModel

/**
 * Created by Nicholas Eddy on 3/22/22.
 */
interface Navigator {
    fun createContact  (name: String, phoneNumber: String)
    fun editContact    (contact: Contact, name: String, phoneNumber: String)
    fun contactDeleted (contact: Contact)
    fun showContact    (contact: Contact)
    fun showContactEdit(contact: Contact)
    fun showContactList()
    fun goBack         ()
}

class NavigatorImpl<M>(private val router: Router, private val contacts: M): Navigator where M: ContactsModel, M: MutableListModel<Contact> {
    override fun createContact(name: String, phoneNumber: String) {
        contacts += Contact(name, phoneNumber)
        showContactList()
        // TODO: Toast creation
    }

    override fun editContact(contact: Contact, name: String, phoneNumber: String) {
        val index = contacts.indexOf(contact)

        when {
            index >= 0 -> contacts[index] = Contact(name, phoneNumber)
        }
        showContactList()
        // TODO: Toast creation
    }

    override fun contactDeleted(contact: Contact) {
        contacts -= contact
        showContactList()
        // TODO: Toast creation
    }

    override fun showContact(contact: Contact) {
        when (val id = contacts.id(contact)) {
             null -> showContactList()
             else -> router.goTo("/contact/$id")
        }
    }

    override fun showContactEdit(contact: Contact) {
        when (val id = contacts.id(contact)) {
            null -> showContactList()
            else -> router.goTo("/contact/$id/edit")
        }
    }

    override fun showContactList() {
        router.goTo("")
    }

    override fun goBack() {
        router.goBack()
    }
}