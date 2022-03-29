package io.nacular.doodle.examples

/**
 * Very simple router that uses a map to track routes.
 */
class TrivialRouter: Router {
    private val routes = mutableMapOf<String, RouteHandler>()

    override fun set(route: String, action: RouteHandler?) {
        when (action) {
            null -> routes.remove(route)
            else -> routes[route] = action
        }
    }

    override fun fireAction() {
//        no-op
    }
}