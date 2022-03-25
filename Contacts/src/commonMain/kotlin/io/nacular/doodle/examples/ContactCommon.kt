package io.nacular.doodle.examples

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

abstract class ContactCommon(
    private val textMetrics: TextMetrics,
    navigator  : Navigator,
    appScope   : CoroutineScope,
    contact    : Contact,
    fonts      : AppFonts,
    buttons    : ButtonFactory,
    modals     : Modals,
): View() {
    protected inner class Avatar(private val name: String): View() {
        override fun render(canvas: Canvas) {
            val circleColor  = name.toColor()
            val firstInitial = "${name.first()}"
            val textSize     = textMetrics.size(firstInitial, font)

            canvas.circle(Circle(radius = min(width, height) / 2, center = Point(width / 2, height / 2)), fill = name.toColor().paint)
            canvas.scale(around = Point(width / 2, height / 2), 4.0, 4.0) {
                text(
                    firstInitial,
                    at    = Point((width - textSize.width) / 2, (height - textSize.height) / 2),
                    color = blackOrWhiteContrast(circleColor),
                    font  = font
                )
            }
        }
    }

    protected val back   = buttons.back()
    protected val name   = Label (contact.name).apply { font = fonts.xLarge }
    protected val avatar = Avatar(contact.name).apply { size = Size(176); font = fonts.medium }
    protected val edit   = buttons.edit  ().apply {
        font = fonts.small
        fired += {
            navigator.showContactEdit(contact)
        }
    }
    protected val delete = buttons.delete().apply {
        font = fonts.small
        fired += {
            appScope.launch {
                if (modals.delete(contact, fonts).show()) {
                    navigator.contactDeleted(contact)
                }
            }
        }
    }

    protected val spacer = view {
        height = 64.0
        render = {
            line(Point(0.0, height / 2), Point(width, height / 2), stroke = Stroke(OUTLINE_COLOR))
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