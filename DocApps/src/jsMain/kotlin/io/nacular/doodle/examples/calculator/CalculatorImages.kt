package io.nacular.doodle.examples.calculator

import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.FrostedGlassPaint
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.examples.Calculator
import io.nacular.doodle.examples.NumberFormatter
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.layout.constraints.constrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalculatorImages(
    display        : Display,
    fonts          : FontLoader,
    textMetrics    : TextMetrics,
    numberFormatter: NumberFormatter,
): Application {
    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        appScope.launch {
            val font = fonts {
                family = "verdana"
                weight = 700
                size   = 30
            }

            val (calculator1, calculator2) = (0..1).map {
                Calculator(fonts, appScope, textMetrics, numberFormatter).apply {
                    enabled = false
                }
            }

            val (glass1, glass2) = (0..1).map { n ->
                object : View() {
                    init {
                        childrenClipPath = object : ClipPath() {
                            override val path get() = bounds.atOrigin.toPath(radius = 40.0)

                            override fun contains(point: Point) = false
                        }
                        val text     = if (n == 0) "Output" else "GridPanel"
                        val textSize = textMetrics.size(text, font)

                        children += view {
                            render = {
                                rect(bounds.atOrigin, fill = FrostedGlassPaint(Transparent, blurRadius = 5.0))
                            }
                        }
                        children += view {

                            suggestSize(textSize.run { Size(width + 3, height + 3) })
                            render = {
                                outerShadow(blurRadius = 3.0) {
                                    text(
                                        text = text,
                                        at   = Point((width - textSize.width) / 2, (height - textSize.height) / 2),
                                        font = font,
                                        fill = White.paint
                                    )
                                }
                            }
                        }

                        layout = constrain(children[0], children[1]) { frost, label ->
                            frost.edges eq parent.edges

                            label.centerY eq if (n == 0) 50 else 60
                            label.centerX eq parent.centerX
                            label.width.preserve
                            label.height.preserve
                        }
                    }
                }
            }

            display += calculator1
            display += calculator2
            display += glass1
            display += glass2

            display.layout = constrain(calculator1, calculator2, glass1, glass2) { c1, c2, g1, g2 ->
                c1.right eq parent.centerX - 20
                c1.width eq (display.children.firstOrNull()?.idealSize?.width ?: 0.0)
                c1.height eq (display.children.firstOrNull()?.idealSize?.height ?: 0.0)
                c1.centerY eq parent.centerY

                c2.left eq parent.centerX + 20
                c2.width eq (display.children.firstOrNull()?.idealSize?.width ?: 0.0)
                c2.height eq (display.children.firstOrNull()?.idealSize?.height ?: 0.0)
                c2.centerY eq parent.centerY

                g1.top eq c1.top + 100
                g1.left eq c1.left + 1//-  10
                g1.right eq c1.right - 1//+  10
                g1.bottom eq c1.bottom - 1//+  10

                g2.top eq c2.top + 1//- 10
                g2.left eq c2.left + 1//- 10
                g2.right eq c2.right - 1//+ 10
                g2.bottom eq g1.top
            }
        }
    }

    override fun shutdown() {}
}