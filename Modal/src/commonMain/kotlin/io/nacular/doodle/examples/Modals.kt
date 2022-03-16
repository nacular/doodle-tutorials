package io.nacular.doodle.examples

import io.nacular.doodle.core.View

/**
 * Simple interface representing a modal view
 */
interface Modal {
    /**
     * Shows the modal
     */
    fun show()

    /**
     * Hides the modal
     */
    fun hide()
}

/**
 * A modal that blocks when shown until a result is produced. This approach
 * is useful when user input is mandatory and the modal cannot be dismissed
 * until the user enters valid input, even if that input is just acknowledgement
 * and the result is `Unit`.
 */
interface SuspendingModal<T> {
    /**
     * Shows the modal and blocks until it has a result.
     */
    suspend fun show(): T
}

/**
 * Provides a way to create Modals without the need to know all their inputs.
 */
interface ModalFactory {
    /**
     * Creates a regular modal by specifying the contents it should display.
     * This allows the modal to be fully customizable.
     *
     * @param contents to display within the modal
     */
    operator fun invoke(contents: (Modal) -> View): Modal

    /**
     * Creates a modal that blocks until it has input.
     *
     * @param contents to display within the modal, and a way to indicate when a result is ready
     */
    operator fun <T> invoke(contents: (completed: (T) -> Unit) -> View): SuspendingModal<T>
}
