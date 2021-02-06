package io.nacular.doodle.examples

import org.w3c.dom.Window

/**
 * Created by Nicholas Eddy on 1/28/21.
 */
class TrivialRouter(private val window: Window): Router {
    private val routes = mutableMapOf<String, RouteHandler>()

    init {
        window.onhashchange = { fireAction() }
    }

    override fun set(route: String, action: RouteHandler?) {
        when (action) {
            null -> routes.remove(route)
            else -> routes[route] = action
        }
    }

    override fun fireAction() {
        val hash = window.location.hash.drop(1)
        routes[hash]?.let { it(hash) }
    }
}