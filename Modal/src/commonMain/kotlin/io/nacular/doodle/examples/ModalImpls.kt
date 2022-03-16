package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animation
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.speedUpSlowDown
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.minusAssign
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.observable
import io.nacular.measured.units.Time
import io.nacular.measured.units.times
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Simple factory to create modals.
 */
class ModalFactoryImpl(private val display: Display, private val animate: Animator): ModalFactory {
    override fun     invoke(contents: (Modal      ) -> View) = ModalImpl          (display, animate, contents)
    override fun <T> invoke(contents: ((T) -> Unit) -> View) = SuspendingModalImpl(display, animate, contents)
}

/**
 * Helper class to render the window within the modal. These modals will render full screen
 * to obscure the content behind them. This dialog will be a child of the modal that contains
 * the contents provided to the [ModalFactory] when the modal was created.
 *
 * @param contents to display in the dialog
 * @param insets applied to contents
 */
private class Dialog(contents: View, insets: Insets = Insets(20.0)): View() {
    init {
        clipCanvasToBounds = false

        children += contents

        layout = object: Layout {
            override fun requiresLayout(child: Positionable, of: PositionableContainer, old: SizePreferences, new: SizePreferences): Boolean {
                return old.idealSize != new.idealSize
            }

            override fun layout(container: PositionableContainer) {
                container.children.forEach {
                    val size = it.idealSize ?: it.size

                    it.bounds = Rectangle(insets.left, insets.top, size.width, size.height)
                    idealSize = Size(size.width + insets.run { left + right }, size.height + insets.run { top + bottom })
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        canvas.outerShadow(vertical = 4.0, blurRadius = 12.0, color = Black.opacity(0.15f)) {
            canvas.rect(bounds.atOrigin, radius = 10.0, color = White)
        }
    }
}

/**
 * Base class for our modals that enables logic reuse.
 *
 * @param display where the modal will be shown
 * @param animate used to animate the modal
 */
abstract class AbstractModal(private val display: Display, private val animate: Animator): View() {
    private var showing      = false
    private var showProgress = 0f
    private var animation: Animation? by observable(null) { old,_ ->
        old?.cancel()
    }

    private val sizeChanged: (Display, Size, Size) -> Unit = { _,_,_ ->
        size = display.size
    }

    fun show(contents: View) {
        if (children.isEmpty()) {
            children += Dialog(contents).apply { opacity = 0f }
            layout = object: Layout {
                override fun requiresLayout(child: Positionable, of: PositionableContainer, old: SizePreferences, new: SizePreferences): Boolean {
                    return old.idealSize != new.idealSize
                }

                override fun layout(container: PositionableContainer) {
                    container.children.forEach {
                        val size = it.idealSize ?: it.size

                        it.bounds = Rectangle((width - size.width) / 2, (height - size.height) / 2, size.width, size.height)
                    }
                }
            }
        }

        if (!showing) {
            showing = true
            size    = display.size

            display += this
            display.sizeChanged += sizeChanged

            animation = (animate(0f to 1f) using speedUpSlowDown(250 * Time.milliseconds)) {
                showProgress = it
                children[0].opacity = it
                rerenderNow()
            }
        }
    }

    fun hide() {
        animation?.cancel()

        children.clear()
        display -= this
        display.sizeChanged -= sizeChanged

        showing  = false
    }

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, color = Black.opacity(0.25f * showProgress))
    }
}

/**
 * Simple modal that displays some content.
 *
 * @param display where the modal will be shown.
 * @param contents to show within the modal.
 */
class ModalImpl(display: Display, animate: Animator, private val contents: (Modal) -> View): AbstractModal(display, animate), Modal {
    override fun show() {
        super.show(contents(this))
    }
}

/**
 * Modal that suspends when shown, until it has a result.
 *
 * @param display where the modal will be shown.
 * @param config defining what the modal shows and when it is complete.
 */
class SuspendingModalImpl<T>(display: Display, animate: Animator, private val config: (completed: (T) -> Unit) -> View): AbstractModal(display, animate), SuspendingModal<T> {
    override suspend fun show(): T = suspendCoroutine { coroutine ->
        try {
            super.show(config {
                super.hide()
                coroutine.resume(it)
            })
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }
}
