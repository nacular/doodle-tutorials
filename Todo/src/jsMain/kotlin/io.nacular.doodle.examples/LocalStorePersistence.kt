package io.nacular.doodle.examples

import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Simple [PersistentStore] based on LocalStore
 */
class LocalStorePersistence: PersistentStore {
    private val name       = "doodle-todos"
    private val serializer = ListSerializer(Task.serializer())

    override fun loadTasks() = when (val stored = localStorage[name]) {
        null -> emptyList()
        else -> Json.decodeFromString(serializer, stored)
    }

    override fun save(tasks: List<Task>) {
        localStorage[name] = Json.encodeToString(serializer, tasks)
    }
}