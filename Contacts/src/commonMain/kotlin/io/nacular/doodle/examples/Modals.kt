package io.nacular.doodle.examples

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.then
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constrain

/**
 * Created by Nicholas Eddy on 3/22/22.
 */
interface Modals {
    fun delete(contact: Contact, fonts: AppFonts): SuspendingModal<Boolean>
}

class ModalsImpl(
    private val modals: ModalFactory,
    private val buttons: ButtonFactory,
): Modals {
    override fun delete(contact: Contact, fonts: AppFonts): SuspendingModal<Boolean> = modals<Boolean> { completed ->
        container {
            font = fonts.small

            this += Label("Delete ${contact.name}?").apply { font = fonts.medium }
            this += buttons.cancel().apply {
                fired += { completed(false) }
            }

            this += buttons.ok().apply {
                fired += { completed(true) }
            }

            layout = constrain(children[0], children[1], children[2]) { text, cancel, ok ->
                text.centerX = parent.centerX

                ok.top  = text.bottom    + INSET
                ok.left = parent.centerX + INSET / 2

                cancel.top   = ok.top
                cancel.right = ok.left - INSET
            }.then {
                idealSize = Size(300.0, children[2].bounds.bottom)
            }
        }
    }
}