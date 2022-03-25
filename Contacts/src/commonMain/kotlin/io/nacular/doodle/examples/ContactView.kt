package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.icons.PathIcon
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.path
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.text.invoke
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.Dimension.Width
import kotlinx.coroutines.CoroutineScope
import kotlin.math.max

class ContactView(
    textMetrics: TextMetrics,
    linkStyler : NativeHyperLinkStyler,
    navigator  : Navigator,
    appScope   : CoroutineScope,
    pathMetrics: PathMetrics,
    contact    : Contact,
    fonts      : AppFonts,
    buttons    : ButtonFactory,
    modals     : Modals): ContactCommon(textMetrics, navigator, appScope, contact, fonts, buttons, modals) {
    init {
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

        setDetail(details)

        layout = simpleLayout {
            layoutCommonItems()
        }
    }
}