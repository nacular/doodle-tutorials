package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animation
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.icons.ImageIcon
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.drawing.text
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.doodle.utils.observable

/**
 * Button use to create a new Contact
 */
class CreateContactButton(
    assets     : AppAssets,
    router     : Router,
    private val animate    : Animator,
    textMetrics: TextMetrics,
): PushButton("Create Contact") {

    private var progress              by renderProperty(0f  )
    private var animation: Animation? by observable    (null) { old,_ ->
        old?.cancel()
    }

    init {
        font   = assets.smallBold
        icon   = ImageIcon(assets.create)
        cursor = Pointer
        fired += {
            router.goTo("/add")
        }

        val iconSize = icon!!.size(this@CreateContactButton)
        acceptsThemes   = false
        iconTextSpacing = 10.0
        behavior        = object: CommonTextButtonBehavior<Button>(textMetrics) {
            override fun clipCanvasToBounds(view: Button) = false

            override fun render(view: Button, canvas: Canvas) {
                val draw     = { canvas.rect(bounds.atOrigin, radius = view.height / 2, color = assets.background) }
                val showText = view.width > view.height

                val shadow = when {
                    showText -> OuterShadow(horizontal = 0.0, vertical = 0.0, color = assets.shadow, blurRadius = 3.0)
                    else     -> OuterShadow(horizontal = 0.0, vertical = 7.0, color = assets.shadow, blurRadius = 5.0)
                }

                canvas.shadow(shadow) {
                    val offset = 3.0 * progress

                    when {
                        view.model.pointerOver && !view.model.pressed || animation != null -> canvas.outerShadow(horizontal = 0.0, vertical = offset, color = assets.shadow, blurRadius = offset) {
                            draw()
                        }
                        else -> draw()
                    }
                }

                when {
                    showText -> {
                        icon!!.render(view, canvas, at = iconPosition(view, icon = icon!!) + Point(x = 10))
                        canvas.text(view.text, at = textPosition(view, icon = icon) + Point(x = 8), font = view.font, color = assets.createButtonText)
                    }
                    else     -> icon!!.render(view, canvas, at = Point((view.width - iconSize.width) / 2, (view.height - iconSize.height) / 2))
                }
            }
        }

        pointerChanged += object: PointerListener {
            override fun entered(event: PointerEvent) {
                super.entered(event)
                animation = (animate (0f to 1f) using assets.fastTransition) {
                    progress = it
                }.apply {
                    completed += {
                        animation = null
                    }
                }
            }

            override fun exited(event: PointerEvent) {
                super.exited(event)
                animation = (animate (1f to 0f) using assets.fastTransition) {
                    progress = it
                }.apply {
                    completed += {
                        animation = null
                    }
                }
            }
        }
    }
}