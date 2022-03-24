package io.nacular.doodle.examples

import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.verticalLayout
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class EditContactView(
    navigator  : Navigator,
    private val textFieldStyler: NativeTextFieldStyler,
    private val pathMetrics: PathMetrics,
    private val appScope: CoroutineScope,
    private val textMetrics: TextMetrics,
    contact    : Contact,
    fonts      : AppFonts,
    buttons    : ButtonFactory,
    modals     : Modals
): View() {
    private inner class Avatar(private val name: String): View() {
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

    init {
        lateinit var newName       : String
        lateinit var newPhoneNumber: String

        val back   = buttons.back()
        val name   = Label (contact.name).apply { font = fonts.xLarge }
        val avatar = Avatar(contact.name).apply { size = Size(176); font = fonts.medium }
        val edit   = buttons.edit  ().apply {
            font = fonts.small
            enabled = false
            fired += {
                navigator.editContact(contact, newName, newPhoneNumber)
            }
        }
        val delete = buttons.delete().apply {
            font = fonts.small
            fired += {
                appScope.launch {
                    if (modals.delete(contact, fonts).show()) {
                        navigator.contactDeleted(contact)
                    }
                }
            }
        }

        val spacer = view {
            height = 64.0
            render = {
                line(Point(0.0, height / 2), Point(width, height / 2), stroke = Stroke(OUTLINE_COLOR))
            }
        }

        val form = Form { this(
                contact.name        to customTextField(textFieldStyler, pathMetrics, "Name",         NAME_ICON_PATH,  Regex(".+"        )),
                contact.phoneNumber to customTextField(textFieldStyler, pathMetrics, "Phone Number", PHONE_ICON_PATH, Regex("[\\s,0-9]+")),
                onInvalid = { edit.enabled = false }
            ) { name_, phone_ ->
                newName        = name_
                newPhoneNumber = phone_
                edit.enabled   = name_ != contact.name || phone_ != contact.phoneNumber
            }
        }.apply {
            font   = fonts.small
            layout = verticalLayout(this, spacing = 32.0, itemHeight = 33.0)
        }

        children += listOf(back, avatar, name, spacer, edit, delete, form)

        layout = Layout.simpleLayout {
            // TODO: Handle smaller width

            back.position   = Point(INSET, 2 * INSET)
            avatar.position = Point(back.bounds.right   + 2 * INSET, back.y)
            name.position   = Point(avatar.bounds.right + 2 * INSET, avatar.bounds.center.y - name.height / 2)

            val idealDeleteX = it.width - INSET - delete.width
            val idealEditX   = idealDeleteX - (edit.width + INSET)

            delete.position = when {
                idealEditX < name.bounds.right + INSET -> Point((it.width + INSET) / 2, avatar.bounds.bottom + INSET / 2)
                else                                   -> Point(idealDeleteX, avatar.bounds.bottom - delete.height)
            }

            spacer.bounds = Rectangle(back.x,   delete.bounds.bottom, max(0.0,   it.width - INSET), spacer.height)
            form.bounds   = Rectangle(spacer.x, spacer.bounds.bottom, min(520.0, spacer.width    ), 98.0         )
            edit.position = Point    (form.x,   form.bounds.bottom + 2 * INSET                                   )
        }
    }

    private operator fun <T> List<T>.component6() = this[5]
}