package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextFit
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.layout.HorizontalFlowLayout
import io.nacular.doodle.layout.ListLayout
import io.nacular.doodle.layout.WidthSource.Parent
import io.nacular.doodle.text.TextDecoration.Companion.UnderLine
import io.nacular.doodle.text.invoke
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.VerticalAlignment.Bottom

/**
 * Contains the links below the task list.
 */
class Footer(private val textMetrics: TextMetrics, private val linkStyler: NativeLinkStyler, private val config: TodoConfig): View() {
    private val textShadow = OuterShadow(vertical = 1.0, blurRadius = 0.0, color = White opacity 0.5f)

    // Plain text
    private fun footerLabel(text: String) = Label(text).apply {
        font            = config.footerFont
        fitText         = setOf(TextFit.Height)
        acceptsThemes   = false
        foregroundColor = config.footerForeground
        behavior        = object: CommonLabelBehavior(textMetrics) {
            override fun render(view: Label, canvas: Canvas) {
                canvas.shadow(textShadow) { super.render(view, this) }
            }
        }
    }

    // Combo text with link at the end
    private fun linkLabel(text: String, linkText: String, url: String) = container {
        val link = HyperLink(url, linkText).apply {
            font            = config.boldFooterFont
            acceptsThemes   = false
            foregroundColor = config.footerForeground
            behavior        = linkStyler(this, object: CommonTextButtonBehavior<HyperLink>(textMetrics) {
                override fun render(view: HyperLink, canvas: Canvas) {
                    // Custom text styling, with underline on pointer-over
                    val styledText = view.foregroundColor { view.font(view.text) }.let {
                        if (view.model.pointerOver) UnderLine { it } else it
                    }

                    canvas.shadow(textShadow) { text(styledText, at = textPosition(view)) }
                }
            }) as Behavior<Button>

            size = textMetrics.size(this.text, font)
        }

        children += listOf(footerLabel(text), link)
        layout    = HorizontalFlowLayout(justification = Center, verticalAlignment = Bottom, horizontalSpacing = 0.0) then {
            height = children.maxOf { it.height }
        }
    }

    init {
        children += footerLabel("Double-click to edit a todo")
        children += linkLabel  ("Created with ", "Doodle", "https://github.com/nacular/doodle")
        children += linkLabel  ("Part of ", "TodoMVC", "http://todomvc.com")

        layout = ListLayout(widthSource = Parent, spacing = 10) then {
            height = children.last().bounds.bottom
        }
    }
}