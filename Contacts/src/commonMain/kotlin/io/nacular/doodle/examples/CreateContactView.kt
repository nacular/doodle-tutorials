package io.nacular.doodle.examples

import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.verticalLayout
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
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
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.min
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import io.nacular.doodle.utils.Dimension.Width
import kotlin.math.min

class CreateContactView(
    private val textFieldStyler: NativeTextFieldStyler,
    private val pathMetrics: PathMetrics,
    private val textMetrics: TextMetrics,
    actions: Navigator,
    fonts: AppFonts,
    buttons: ButtonFactory,
    image: Image
): View() {
    private inner class Avatar(private val image: Image): View() {
        var name by renderProperty("")

        override fun render(canvas: Canvas) {
            when {
                name.isBlank() -> canvas.clip(Circle(radius = min(width, height) / 2, center = Point(width / 2, height / 2))) {
                    canvas.image(image, destination = bounds.atOrigin)
                }
                else -> {
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
        }
    }

    init {
        lateinit var name       : String
        lateinit var phoneNumber: String

        val label = Label("Create Contact").apply {
            font = fonts.medium
            height = 28.0
            fitText = setOf(Width)
        }

        val back   = buttons.back()
        val avatar = Avatar(image).apply { size = Size(176); font = fonts.medium }
        val button = buttons.create().apply {
            font    = fonts.small
            enabled = false

            fired += {
                actions.createContact(name, phoneNumber)
            }
        }

        val form = Form { this(
                + customTextField(textFieldStyler, pathMetrics, "Name",         NAME_ICON_PATH,  Regex(".+"        )) { textChanged += { _,_,new -> avatar.name = new } },
                + customTextField(textFieldStyler, pathMetrics, "Phone Number", PHONE_ICON_PATH, Regex("[\\s,0-9]+")),
                onInvalid = { button.enabled = false }
            ) { name_, phone_ ->
                name        = name_
                phoneNumber = phone_
                button.enabled = true
            }
        }.apply {
            font   = fonts.small
            layout = verticalLayout(this, spacing = 32.0, itemHeight = 33.0)
        }

        val spacer = view {
            height = 64.0
            render = {
                line(Point(0.0, height / 2), Point(width, height / 2), stroke = Stroke(OUTLINE_COLOR))
            }
        }

        children += listOf(label, back, avatar, spacer, form, button)

        layout = constrain(label, back, avatar, spacer, form, button) { (label, back, avatar, spacer, form, button) ->
            label.top    = parent.top  + 2 * INSET
            label.left   = parent.left + INSET

            back.top     = label.bottom + 2 * INSET
            back.left    = label.left

            avatar.top   = label.bottom + 3.0/2 * INSET
            avatar.left  = back.right   + 2 * INSET

            spacer.top   = avatar.bottom
            spacer.left  = back.left
            spacer.right = parent.right

            form.top     = spacer.bottom
            form.left    = back.left
            form.width   = min(parent.width - INSET, constant(520.0))

            button.top   = form.bottom + 2 * INSET
            button.left  = back.left
        }
    }

    private operator fun <T> List<T>.component6() = this[5]
}
