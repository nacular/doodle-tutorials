package io.nacular.doodle.examples

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * Base class for Contact views that handles common setup and rendering.
 */
abstract class ContactCommon(
    modals      : Modals,
    assets      : AppAssets,
    buttons     : AppButtons,
    contact     : Contact,
    appScope    : CoroutineScope,
    navigator   : Navigator,
    textMetrics : TextMetrics,
    uiDispatcher: CoroutineDispatcher,
): View() {
    protected val back   = buttons.back(assets.backIcon)
    protected val name   = Label (contact.name).apply { font = assets.xLarge }
    protected val avatar = Avatar(textMetrics, contact.name).apply { size = Size(176); font = assets.medium }
    protected val edit   = buttons.edit(assets.buttonBackground, assets.buttonForeground).apply {
        font = assets.small
        fired += {
            navigator.showContactEdit(contact)
        }
    }

    protected val delete = buttons.delete(assets.deleteBackground, assets.buttonForeground).apply {
        font = assets.small
        fired += {
            appScope.launch(uiDispatcher) {
                if (modals.delete(assets, contact).show()) {
                    navigator.contactDeleted(contact)
                }
            }
        }
    }

    protected val spacer = view {
        height = 64.0
        render = {
            line(Point(0.0, height / 2), Point(width, height / 2), stroke = Stroke(assets.outline))
        }
    }

    private lateinit var details: View

    init {
        children += listOf(back, avatar, name, spacer, edit, delete)
    }

    private operator fun <T> List<T>.component6() = this[5]

    protected fun setDetail(view: View) {
        details   = view
        children += view
    }

    protected fun layoutCommonItems() {
        // TODO: Handle smaller width

        back.position   = Point(INSET, 2 * INSET)
        avatar.position = Point(back.bounds.right + 2 * INSET, back.y)
        name.position   = Point(avatar.bounds.right + 2 * INSET, avatar.bounds.center.y - name.height / 2)

        val idealDeleteX = width - INSET - delete.width
        val idealEditX   = idealDeleteX - (edit.width + INSET)

        if (idealEditX < name.bounds.right + INSET) {
            delete.position = Point(width / 2 + INSET / 2, avatar.bounds.bottom + INSET / 2)
            edit.position   = Point(delete.x - (edit.width + INSET), delete.y)
        } else {
            delete.position = Point(idealDeleteX, avatar.bounds.bottom - delete.height)
            edit.position   = Point(idealEditX,   delete.y                            )
        }

        spacer.bounds  = Rectangle(back.x, delete.bounds.bottom, max(0.0, width - 2 * INSET), spacer.height)
        details.bounds = Rectangle(spacer.x, spacer.bounds.bottom, min(520.0, spacer.width), 98.0)
    }
}