package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.utils.observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Base class for Contact views that handles common setup and rendering.
 */
abstract class ContactCommon(
    modals      : Modals,
    assets      : AppConfig,
    buttons     : AppButtons,
    contact     : Contact,
    contacts    : ContactsModel,
    appScope    : CoroutineScope,
    navigator   : Navigator,
    textMetrics : TextMetrics,
    uiDispatcher: CoroutineDispatcher,
): View() {
    protected var contact: Contact by observable(contact) { _,new ->
        name.text   = new.name
        avatar.name = new.name
    }

    protected val back   = buttons.back(assets.backIcon)
    protected val name   = Label (contact.name).apply { font = assets.xLarge }
    protected val avatar = Avatar(textMetrics, contact.name).apply { suggestSize(Size(176)); font = assets.medium }
    protected val edit   = buttons.edit(assets.buttonBackground, assets.buttonForeground).apply { font = assets.small }
    protected val delete = buttons.delete(assets.deleteBackground, assets.buttonForeground).apply {
        font = assets.small
        fired += {
            appScope.launch(uiDispatcher) {
                if (modals.confirmDelete(assets, this@ContactCommon.contact)) {
                    contacts -= this@ContactCommon.contact
                    navigator.showContactList()
                }
            }
        }
    }

    protected val spacer = view {
        suggestHeight(64.0)

        render = {
            line(Point(0.0, height / 2), Point(width, height / 2), stroke = Stroke(assets.outline))
        }
    }

    private lateinit var details: View

    init {
        children += listOf(back, avatar, name, spacer, edit, delete)
    }

    protected fun setDetail(view: View) {
        details   = view
        children += view
    }

    protected fun layoutCommonItems(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets = None) {
        val (back, avatar, name, spacer, edit, delete, details) = views.toList()

        back.updatePosition  (INSET, 2 * INSET)
        avatar.updatePosition(back.bounds.right + 2 * INSET, back.bounds.y)
        name.updatePosition  (avatar.bounds.right + 2 * INSET, avatar.bounds.center.y - name.bounds.height / 2)

        val idealDeleteX = width - INSET - delete.bounds.width
        val idealEditX   = idealDeleteX - (edit.bounds.width + INSET)

        when {
            idealEditX < name.bounds.right + INSET -> {
                avatar.updatePosition((width - avatar.bounds.width) / 2, back.bounds.y)
                name.updatePosition  (avatar.bounds.center.x - name.bounds.width / 2, avatar.bounds.bottom + INSET)
                delete.updatePosition(width / 2 + INSET / 2, name.bounds.bottom + INSET)
                edit.updatePosition  (delete.bounds.x - (edit.bounds.width + INSET), delete.bounds.y)
            }
            else                                   -> {
                delete.updatePosition(idealDeleteX, avatar.bounds.bottom - delete.bounds.height)
                edit.updatePosition  (idealEditX,   delete.bounds.y                            )
            }
        }

        spacer.updateBounds (Rectangle(back.bounds.x, delete.bounds.bottom, width - 2 * INSET, spacer.bounds.height))
        details.updateBounds(Rectangle(spacer.bounds.x, spacer.bounds.bottom, min(520.0, spacer.bounds.width), 98.0))
    }
}