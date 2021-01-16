package io.nacular.doodle.examples

import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.NativeTextFieldBehavior
import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Creates a [TodoApp]
 */
class LocalStorePersistence: PersistentStore {
    private val name = "doodle-todos"

    override fun loadTasks() = when (val stored = localStorage[name]) {
        null -> emptyList()
        else -> Json.decodeFromString(ListSerializer(Task.serializer()), stored)
    }

    override fun save(tasks: List<Task>) {
        localStorage[name] = Json.encodeToString(ListSerializer(Task.serializer()), tasks)
    }
}

fun main() {
    application(modules = listOf(
        FontModule,
        PointerModule,
        KeyboardModule,
        basicLabelBehavior(foregroundColor = Color(0x4D4D4Du)),
        NativeTextFieldBehavior,
        Module(name = "AppModule") {
            bind<ImageLoader    >() with singleton { ImageLoaderImpl      (instance(), instance()) }
            bind<PersistentStore>() with singleton { LocalStorePersistence(                      ) }
        }
    )) {
        // load app
        TodoApp(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}