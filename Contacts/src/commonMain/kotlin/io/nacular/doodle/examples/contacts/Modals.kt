package io.nacular.doodle.examples.contacts

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.invoke
import io.nacular.doodle.animation.transition.easeInOutCubic
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.modal.ModalManager
import io.nacular.doodle.controls.modal.ModalManager.Modal
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.simpleTextButtonRenderer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lerp
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times

/**
 * Provides a way to show common app modals.
 */
interface Modals {
    /**
     * Shows the Delete Contact confirmation modal.
     *
     * @param assets containing fonts, colors, etc.
     * @param contact being deleted
     */
    suspend fun confirmDelete(assets: AppConfig, contact: Contact): Boolean
}

class ModalsImpl(
    private val textMetrics: TextMetrics,
    private val modals     : ModalManager,
    private val animate    : Animator
): Modals {

    private fun button(text: String, foreground: Color) = PushButton(text).apply {
        size          = Size(113, 40)
        cursor        = Pointer
        acceptsThemes = false
        behavior      = simpleTextButtonRenderer(textMetrics) { button, canvas ->
            when {
                button.model.pointerOver -> canvas.rect(bounds.atOrigin, color = Lightgray)
                else                     -> canvas.line(Point(0.0, 0.5), Point(width, 0.5), stroke = Stroke(Lightgray))
            }

            canvas.text(button.text, at = textPosition(button, button.text), fill = foreground.paint, font = font)
        }
    }

    override suspend fun confirmDelete(assets: AppConfig, contact: Contact): Boolean = modals {
        animate(0f to 1f, using = tweenFloat(easeInOutCubic, 250 * milliseconds)) {
            background = lerp(Black opacity 0f, Black opacity 0.25f, it).paint
        }

        Modal(
            object: View() {
                private val clipPath get() = object: ClipPath(bounds.atOrigin.toPath(10.0)) {
                    override fun contains(point: Point) = point in bounds.atOrigin
                }

                init {
                    font               = assets.small
                    childrenClipPath   = clipPath
                    clipCanvasToBounds = false

                    children += Label ("Delete contact?").apply { font = assets.medium }
                    children += button("Cancel", assets.createButtonText).apply { fired += { completed(false) } }
                    children += button("Ok", assets.deleteBackground).apply { fired += { completed(true) } }

                    boundsChanged += { _,old,new ->
                        if (new.size != old.size) {
                            childrenClipPath = clipPath
                        }
                    }

                    layout = constrain(children[0], children[1], children[2]) { text, cancel, ok ->
                        text.top     eq 20
                        text.height  eq text.height.readOnly
                        text.centerX eq parent.centerX

                        ok.top   eq text.bottom + INSET
                        ok.right eq parent.right
                        ok.width eq parent.width / 2

                        cancel.top   eq ok.top
                        cancel.left  eq 0
                        cancel.width eq ok.width
                    }.then {
                        size = Size(300.0, children[2].bounds.bottom)
                    }
                }

                override fun render(canvas: Canvas) {
                    canvas.outerShadow(vertical = 4.0, blurRadius = 12.0, color = Black opacity 0.15f) {
                        canvas.rect(bounds.atOrigin, radius = 10.0, color = White)
                    }
                }
            }
        )
    }
}