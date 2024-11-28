package io.nacular.doodle.examples.calculator

import io.nacular.doodle.HtmlElementViewFactory
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Color
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

interface GlassPanelFactory {
    operator fun invoke(color: Color, blurRadius: Double): View
}

class GlassPanelFactoryImpl(private val htmlViews: HtmlElementViewFactory): GlassPanelFactory {
    override fun invoke(color: Color, blurRadius: Double): View = htmlViews(
        (document.createElement("div") as HTMLElement).apply {
            style.backgroundColor = color.run { "rgba($red,$green,$blue,$opacity)" }
            style.setProperty("-webkit-backdrop-filter", "blur(${blurRadius}px)")
            style.setProperty("backdrop-filter", "blur(${blurRadius}px)")
        }
    )
}