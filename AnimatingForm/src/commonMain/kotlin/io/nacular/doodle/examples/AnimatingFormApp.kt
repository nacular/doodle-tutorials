package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.examples.AnimatingFormApp.AppFonts
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.theme.Theme
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Simple app that places a [AnimatingForm] at the center of the display.
 */
//sampleStart
class AnimatingFormApp(
    display        : Display,
    fonts          : FontLoader,
    animator       : Animator,
    textMetrics    : TextMetrics,
    pathMetrics    : PathMetrics,
    theme          : Theme,
    themeManager   : ThemeManager,
    textFieldStyler: NativeTextFieldStyler,
): Application {
    data class AppFonts(val header: Font, val body: Font, val button: Font, val textField: Font)

    init {
        themeManager.selected = theme

        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        appScope.launch {
            // creat and display the animating form
            with(display) {
                this += AnimatingForm(
                    fonts           = appFonts(fonts),
                    animate         = animator,
                    pathMetrics     = pathMetrics,
                    textMetrics     = textMetrics,
                    textFieldStyler = textFieldStyler,
                )

                layout = constrain(first(), fill(Insets(50.0)))

                fill(Lightgray.paint)
            }
        }
    }

    override fun shutdown() { /* no-op */ }
}
//sampleEnd

private suspend fun appFonts(fonts: FontLoader): AppFonts {
    val headerFont = fonts("gothampro_medium.ttf") {
        family = "GothamPro Medium"
        weight = 700
        size   = 38
    }!!

    val bodyFont = fonts("gothampro_light.ttf") {
        family = "GothamPro Light"
        size   = 18
    }!!

    val buttonFont = fonts(headerFont) {
        size = 16
    }!!

    val textFieldFont = fonts(bodyFont) {
        size   = 14
        weight = 400
    }!!

    return AppFonts(body = bodyFont, header = headerFont, textField = textFieldFont, button = buttonFont)
}