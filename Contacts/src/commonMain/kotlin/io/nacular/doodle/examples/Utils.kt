package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.form.FieldVisualizer
import io.nacular.doodle.controls.form.field
import io.nacular.doodle.controls.form.textField
import io.nacular.doodle.controls.icons.PathIcon
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.theme.simpleTextButtonRenderer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.grayScale
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.path
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.doodle.theme.native.NativeTextFieldBehaviorModifier
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlin.math.floor

// Simple string to color mapper from: https://github.com/zenozeng/color-hash/blob/main/lib/bkdr-hash.ts
fun String.toColor(): Color {
    val seed           = 131
    val seed2          = 237
    var hash           = 0
    val str            = this + 'x' // make hash more sensitive for short string like 'a', 'b', 'c'
    val maxSafeInteger = floor((0xffffffu / seed2.toUInt()).toDouble())

    str.forEach {
        if (hash > maxSafeInteger) {
            hash = floor((hash / seed2).toDouble()).toInt()
        }
        hash = hash * seed + it.code
    }

    return Color(hash.toUInt())
}

interface ButtonFactory {
    fun ok    (): Button
    fun back  (): Button
    fun edit  (): Button
    fun delete(): Button
    fun create(): Button
    fun cancel(): Button
}

class ButtonFactoryImpl(
    private val textMetrics: TextMetrics,
    private val pathMetrics: PathMetrics,
    private val navigator  : Navigator
): ButtonFactory {
    override fun back(): Button {
        return PathIconButton(
            pathData    = "M18.6903 8.177H4.4975l6.5417-6.5417L9.3452 0 0 9.3452l9.3452 9.3452 1.6352-1.6353-6.4829-6.5417H18.6903v-2.3363z",
            pathMetrics = pathMetrics
        ).apply {
            size            = Size(28)
            cursor          = Pointer
            foregroundColor = Black
            fired += {
                navigator.goBack()
            }
        }
    }

    override fun ok    () = simpleButton("Ok",     background = BUTTON_BLUE, text = White)
    override fun edit  () = simpleButton("Edit",   background = BUTTON_BLUE, text = White)
    override fun create() = simpleButton("Create", background = BUTTON_BLUE, text = White)
    override fun delete() = simpleButton("Delete", background = Color(221u, 66u, 53u), text = White)
    override fun cancel() = simpleButton("Cancel", background = BUTTON_BLUE, text = White)

    private fun simpleButton(name: String, background: Color, text: Color) = PushButton(name).apply {
        size = Size(113, 40)

        behavior = simpleTextButtonRenderer(textMetrics) { button, canvas ->
            val color = background.let { if (enabled) it else it.grayScale() }

            canvas.rect(bounds.atOrigin, radius = 4.0, fill = color.paint)
            canvas.text(button.text, at = textPosition(button, button.text), fill = text.paint, font = font)
        }
    }
}

fun customTextField(textFieldStyler: NativeTextFieldStyler, pathMetrics: PathMetrics, placeHolder: String, icon: String, regex: Regex, config: TextField.() -> Unit = {}) = iconField(pathMetrics, icon) {
    textField(regex) {
        textField.placeHolder      = placeHolder
        textField.placeHolderColor = PLACE_HOLDER_COLOR
        textField.behavior         = textFieldStyler(textField, customTextFieldBehavior(textField, OUTLINE_COLOR))

        config(textField)
    }
}

private fun <T> iconField(pathMetrics: PathMetrics, path: String, visualizer: () -> FieldVisualizer<T>) = field<T> {
    container {
        val icon     = PathIcon<View>(path(path), pathMetrics = pathMetrics)
        val iconSize = icon.size(this)
        focusable    = false

        this += visualizer()(this@field).also {
            it.sizePreferencesChanged += { _, _, _ ->
                relayout()
            }
        }

        foregroundColor = Black
        render = {
            icon.render(this@container, this, at = Point(0.0, (height - iconSize.height) / 2))
        }

        layout = constrain(children[0]) {
            fill(Insets(left = iconSize.width + 24))(it)
        }
    }
}

private fun customTextFieldBehavior(textField: TextField, color: Color) = object: NativeTextFieldBehaviorModifier {
    init {
        textField.acceptsThemes   = false
        textField.borderVisible   = false
        textField.backgroundColor = Transparent
    }

    override fun renderBackground(textField: TextField, canvas: Canvas) {
        canvas.line(start = Point(0.0, textField.height - 1.0), end = Point(textField.width, textField.height - 1.0), Stroke(thickness = 1.0, fill = color.paint))
    }
}

// FIXME: Hack to run on desktop b/c of https://github.com/JetBrains/skiko/issues/518
var DESKTOP_WORK_AROUND = false

/*const*/ val NAME_ICON_PATH  get() = if (DESKTOP_WORK_AROUND) "M1 16c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V4H1v12zM14 1h-3.5l-1-1h-5l-1 1H0v2h14V1z" else "M8 1.9a2.1 2.1 0 110 4.2 2.1 2.1 0 010-4.2m0 9c2.97 0 6.1 1.46 6.1 2.1v1.1H1.9V13c0-.64 3.13-2.1 6.1-2.1M8 0C5.79 0 4 1.79 4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 9c-2.67 0-8 1.34-8 4v3h16v-3c0-2.66-5.33-4-8-4z"
/*const*/ val PHONE_ICON_PATH get() = if (DESKTOP_WORK_AROUND) "M1 16c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V4H1v12zM14 1h-3.5l-1-1h-5l-1 1H0v2h14V1z" else "M3.54 2c.06.89.21 1.76.45 2.59l-1.2 1.2c-.41-1.2-.67-2.47-.76-3.79h1.51m9.86 12.02c.85.24 1.72.39 2.6.45v1.49c-1.32-.09-2.59-.35-3.8-.75l1.2-1.19M4.5 0H1c-.55 0-1 .45-1 1 0 9.39 7.61 17 17 17 .55 0 1-.45 1-1v-3.49c0-.55-.45-1-1-1-1.24 0-2.45-.2-3.57-.57a.84.84 0 00-.31-.05c-.26 0-.51.1-.71.29l-2.2 2.2a15.149 15.149 0 01-6.59-6.59l2.2-2.2c.28-.28.36-.67.25-1.02A11.36 11.36 0 015.5 1c0-.55-.45-1-1-1z"
const val INSET           =  16.0

val OUTLINE_COLOR      = Color(229u, 231u, 235u)
val PLACE_HOLDER_COLOR = Color(156u, 153u, 155u)

private val BUTTON_BLUE = Color(26u, 115u, 232u)
