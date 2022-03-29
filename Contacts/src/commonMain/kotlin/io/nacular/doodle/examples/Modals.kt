package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.simpleTextButtonRenderer
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.Cursor.Companion.Pointer

interface Modals {
    fun delete(assets: AppAssets, contact: Contact): SuspendingModal<Boolean>
}

class ModalsImpl(private val textMetrics: TextMetrics, private val modals: ModalFactory): Modals {
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

    override fun delete(assets: AppAssets, contact: Contact): SuspendingModal<Boolean> = modals<Boolean>(insets = Insets(top = 20.0)) { completed ->
        container {
            font = assets.small

            this += Label ("Delete contact?"                ).apply { font   = assets.medium        }
            this += button("Cancel", assets.createButtonText).apply { fired += { completed(false) } }
            this += button("Ok",     assets.deleteBackground).apply { fired += { completed(true ) } }

            layout = constrain(children[0], children[1], children[2]) { text, cancel, ok ->
                text.centerX = parent.centerX

                ok.top       = text.bottom + INSET
                ok.right     = parent.right
                ok.width     = parent.width / 2

                cancel.top   = ok.top
                cancel.left  = parent.left
                cancel.width = ok.width
            }.then {
                idealSize = Size(300.0, children[2].bounds.bottom)
            }
        }
    }
}