package io.nacular.doodle.examples

import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import kotlinx.serialization.Serializable

interface PersistentStore {
    fun loadTasks(                 ): List<Task>
    fun save     (tasks: List<Task>)
}

@Serializable
class Task(val text: String, val completed: Boolean = false)

interface DataStore {
    val size     : Int        get() = tasks.size
    val tasks    : List<Task>
    val active   : List<Task> get() = tasks.filter { !it.completed }
    val isEmpty  : Boolean    get() = tasks.isEmpty()
    val completed: List<Task> get() = tasks.filter { it.completed  }

    val tasksChanged: Pool<ChangeObserver<DataStore>>

    fun add             (value: String): Task
    fun remove          (value: Task  )
    fun markActive      (value: Task  )
    fun markCompleted   (value: Task  )
    fun markAllActive   (             )
    fun markAllCompleted(             )
    fun removeCompleted (             )
}

interface DataStoreListModel: DataStore, MutableListModel<Task> {
    override val size   : Int     get() = super.size
    override val isEmpty: Boolean get() = super<DataStore>.isEmpty
}

class SimpleDataStore private constructor(override val tasks: ObservableList<Task>): SimpleMutableListModel<Task>(tasks), DataStoreListModel {

    override val tasksChanged = SetPool<ChangeObserver<DataStore>>()

    init {
        tasks.changed += { _,_,_,_ -> tasksChanged.forEach { it(this) } }
    }

    override fun add(value: String) = Task(value).also { super.add(it) }

    override fun set(index: Int, value: Task) = when {
        value.text.isBlank() -> removeAt(index)
        else                 -> super.set(index, value)
    }

    override fun markAllCompleted() {
        tasks.batch {
            forEachIndexed { index, item ->
                set(index, Task(item.text, completed = true))
            }
        }
    }

    override fun markAllActive() {
        tasks.batch {
            forEachIndexed { index, item ->
                set(index, Task(item.text, completed = false))
            }
        }
    }

    override fun markActive(value: Task) {
        tasks.indexOf(value).takeIf { it > -1 }?.let {
            set(it, Task(value.text, completed = false))
        }
    }

    override fun markCompleted(value: Task) {
        tasks.indexOf(value).takeIf { it > -1 }?.let {
            set(it, Task(value.text, completed = true))
        }
    }

    override fun removeCompleted() {
        tasks.removeAll(completed)
    }

    override val isEmpty get() = super<SimpleMutableListModel>.isEmpty
    override val size    get() = super<SimpleMutableListModel>.size

    companion object {
        operator fun invoke(list: List<Task> = emptyList()): SimpleDataStore = SimpleDataStore(ObservableList(list))
    }
}