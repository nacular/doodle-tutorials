package io.nacular.doodle.examples.contacts

interface PersistentStore<T> {
    fun load(              ): List<T>
    fun save(tasks: List<T>)
}