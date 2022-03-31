import io.nacular.doodle.examples.contacts.RouteHandler

/**
 * Simple Router that does not modify the window hash.
 */
class EmbeddedRouter: io.nacular.doodle.examples.contacts.Router {
    private val routes  = LinkedHashMap<Regex, RouteHandler>()
    private val history = mutableListOf<String>()

    override fun set(route: String, action: RouteHandler?) {
        when (action) {
            null -> routes.remove(Regex(route))
            else -> routes[Regex(route)] = action
        }
    }

    override fun goTo(route: String) {
        history += route
        fireAction()
    }

    override fun fireAction() {
        val route = history.lastOrNull() ?: ""

        routes.forEach { (regex, handler) ->
            regex.matchEntire(route)?.let {
                handler.invoke(route, it.groupValues.drop(1))
                return
            }
        }
    }

    override fun goBack() {
        history.removeLastOrNull()?.let {
            fireAction()
        }
    }
}