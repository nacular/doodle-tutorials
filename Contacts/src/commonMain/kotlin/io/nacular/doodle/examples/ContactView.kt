package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.icons.PathIcon
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
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
import io.nacular.doodle.geometry.path
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.text.invoke
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.Dimension.Width
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class ContactView(
    private val textMetrics: TextMetrics,
    linkStyler          : NativeHyperLinkStyler,
    navigator  : Navigator,
    appScope   : CoroutineScope,
    pathMetrics: PathMetrics,
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
        val back   = buttons.back()
        val name   = Label (contact.name).apply { font = fonts.xLarge }
        val avatar = Avatar(contact.name).apply { size = Size(176); font = fonts.medium }
        val edit   = buttons.edit  ().apply {
            font = fonts.small
            fired += {
                navigator.showContactEdit(contact)
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

        val details = container {
            this += Label("Contact Details").apply {
                font = fonts.small
                height = 24.0
                fitText = setOf(Width)
            }
            this += HyperLink(
                url  = "tel:${contact.phoneNumber}",
                text = contact.phoneNumber,
                icon = PathIcon(path = path(PHONE_ICON_PATH), pathMetrics = pathMetrics, fill = Black),
            ).apply {
                font            = fonts.small
                acceptsThemes   = false
                iconTextSpacing = 16.0
                behavior        = linkStyler(this, object: CommonTextButtonBehavior<HyperLink>(textMetrics) {
                    override fun install(view: HyperLink) {
                        super.install(view)
                        val textSize = textMetrics.size(text, font)
                        val iconSize = icon!!.size(view)

                        size = Size(textPosition(view).x + textSize.width, max(iconSize.height, textSize.height))
                    }

                    override fun render(view: HyperLink, canvas: Canvas) {
                        icon!!.render(view, canvas, at = iconPosition(view, icon = icon!!))
                        canvas.text(Color(59u, 130u, 246u).invoke { view.font(view.text) }, at = textPosition(view))
                    }
                }) as Behavior<Button>
            }

            render = {
                rect(bounds.atOrigin.inset(0.5), radius = 12.0, stroke = Stroke(OUTLINE_COLOR))
            }

            layout = constrain(children[0], children[1]) { label, link ->
                label.top  = parent.top  + INSET
                label.left = parent.left + INSET

                link.top  = label.bottom + INSET
                link.left = label.left
            }
        }

        children += listOf(back, avatar, name, spacer, edit, delete, details)

        layout = Layout.simpleLayout {
            // TODO: Handle smaller width

            back.position   = Point(INSET, 2 * INSET)
            avatar.position = Point(back.bounds.right + 2 * INSET, back.y)
            name.position   = Point(avatar.bounds.right + 2 * INSET, avatar.bounds.center.y - name.height / 2)

            val idealDeleteX = it.width - INSET - delete.width
            val idealEditX   = idealDeleteX - (edit.width + INSET)

            if (idealEditX < name.bounds.right + INSET) {
                delete.position = Point(it.width / 2 + INSET / 2, avatar.bounds.bottom + INSET / 2)
                edit.position   = Point(delete.x - (edit.width + INSET), delete.y)
            } else {
                delete.position = Point(idealDeleteX, avatar.bounds.bottom - delete.height)
                edit.position   = Point(idealEditX,   delete.y                            )
            }

            spacer.bounds  = Rectangle(back.x,   delete.bounds.bottom, max(0.0,   it.width - 2 * INSET), spacer.height)
            details.bounds = Rectangle(spacer.x, spacer.bounds.bottom, min(520.0, spacer.width        ), 98.0         )
        }
    }

    private operator fun <T> List<T>.component6() = this[5]
}