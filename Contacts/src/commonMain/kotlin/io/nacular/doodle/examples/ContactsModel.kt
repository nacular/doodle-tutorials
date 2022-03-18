package io.nacular.doodle.examples

import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.utils.FilteredList
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.observable

/**
 * Data representing a contact
 */
data class Contact(val name: String, val phoneNumber: String)

/**
 * Collection of contacts
 */
interface ContactsModel {
    /**
     * Changes which contacts are shown
     */
    var filter: ((Contact) -> Boolean)?

    operator fun plusAssign (contact: Contact)
    operator fun minusAssign(contact: Contact)
}

/**
 * Model based on [FilteredList]
 */
class SimpleContactsModel(
    private val contacts    : ObservableList<Contact> = ObservableList(),
    private val filteredList: FilteredList<Contact> = FilteredList(contacts)
): SimpleMutableListModel<Contact>(filteredList), ContactsModel {

    override var filter: ((Contact) -> Boolean)? by observable(null) { _,new ->
        filteredList.filter = new
    }

    override fun plusAssign (contact: Contact) = super.add   (contact)
    override fun minusAssign(contact: Contact) = super.remove(contact)
}
