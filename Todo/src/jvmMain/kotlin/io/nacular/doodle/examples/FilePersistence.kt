package io.nacular.doodle.examples

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Simple [PersistentStore] based on a file
 */
class FilePersistence: PersistentStore {
    private val file       = File("doodle-todos.json")
    private val serializer = ListSerializer(Task.serializer())

    override fun loadTasks(): List<Task> {
        try {
            return Json.decodeFromString(serializer, file.readText())
        } catch (ignored: Exception) {
        }

        return emptyList()
    }

    override fun save(tasks: List<Task>) {
        file.writeText(Json.encodeToString(serializer, tasks))
    }
}