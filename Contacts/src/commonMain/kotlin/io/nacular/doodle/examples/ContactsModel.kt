package io.nacular.doodle.examples

import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.utils.FilteredList
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.observable
import kotlinx.serialization.Serializable

/**
 * Data representing a contact
 */
@Serializable
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

    fun id(of: Contact): Int?
    fun find(id: Int): Contact?
}

interface PersistentStore<T> {
    fun load(              ): List<T>
    fun save(tasks: List<T>)
}

/**
 * Model based on [FilteredList]
 */
class SimpleContactsModel private constructor(
    private val contacts    : ObservableList<Contact> = ObservableList(),
    private val filteredList: FilteredList<Contact> = FilteredList(contacts)
): SimpleMutableListModel<Contact>(filteredList), ContactsModel {
    override var filter: ((Contact) -> Boolean)? by observable(null) { _,new ->
        filteredList.filter = new
    }

    override fun plusAssign (contact: Contact) = if (size > 0) super.add(0, contact) else super.add(contact)
    override fun minusAssign(contact: Contact) = super.remove(contact)

    override fun id(of: Contact): Int? = indexOf(of).takeIf { it >= 0 }

    override fun find(id: Int): Contact? = this[id].getOrNull()

    companion object {
        operator fun invoke(persistentStore: PersistentStore<Contact>): SimpleContactsModel {
            val tasks = ObservableList(persistentStore.load()).apply {
                changed += { _,_,_,_ ->
                    persistentStore.save(this)
                }
            }

            return SimpleContactsModel(tasks, FilteredList(tasks)).apply {
                // Add dummy data if empty
                if (isEmpty) {
                    this += Contact("Joe",             "1234567")
                    this += Contact("Jack",            "1234567")
                    this += Contact("Bob",             "1234567")
                    this += Contact("Jen",             "1234567")
                    this += Contact("Herman",          "1234567")
                    this += Contact("Lisa Fuentes",    "1234567")
                    this += Contact("Langston Hughes", "1234567")
                }
            }
        }
    }
}
