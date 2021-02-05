package io.nacular.doodle.examples

/**
 * Created by Nicholas Eddy on 1/28/21.
 */
typealias RouteHandler = ((path: String) -> Unit)

interface Router {
    operator fun set(route: String, action: RouteHandler?)

    fun notify()
}