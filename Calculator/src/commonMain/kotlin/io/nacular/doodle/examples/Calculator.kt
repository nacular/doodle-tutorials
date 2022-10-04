package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ButtonGroup
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.panels.GridPanel
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Darkgray
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.Orange
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.doodle.utils.roundToNearest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.js.JsName
import kotlin.math.pow

/**
 * Simple calculator with basic math operations.
 *
 * @property fonts           used to find fonts
 * @param    appScope        used to run coroutines
 * @property textMetrics     used to measure text
 * @property numberFormatter used to display number output
 */
class Calculator(
        private val fonts          : FontLoader,
                    appScope       : CoroutineScope,
        private val textMetrics    : TextMetrics,
        private val numberFormatter: NumberFormatter
): View() {
    // region ================ Helper Classes ==================================

    private inner class Output: View() {

        // Default width when input starts at `0`
        private val defaultWidth get() = textMetrics.width("0", font)

        // Text inset from the left/right edge of the output
        private val inset by lazy { (clear.width - defaultWidth) / 2 }

        // Transform used to scale text down as it grows beyond window width
        private var textTransform = Identity

        /** Numeric value of the output */
        var number = 0.0
            set(new) {
                field = new
                text  = numberFormatter(number)
            }

        /** Text representation of [number] */
        var text = "0"
            set(new) {
                field = new

                val textWidth   = textMetrics.width(field, font)
                val windowWidth = width - inset * 2

                // use transform when text grows beyond window width
                textTransform = when {
                    textWidth > windowWidth -> (windowWidth/textWidth).let { Identity.scale(x = it, y = it, around = Point(width / 2, height)) }
                    else                    -> Identity
                }

                rerender()
            }

        init {
            foregroundColor = White
        }

        override fun render(canvas: Canvas) {
            val textPosition = textMetrics.size(text, font).let {
                val x = when {
                    textTransform.isIdentity -> width - it.width - inset
                    else                     -> (width - it.width) / 2
                }

                Point(x, height - it.height)
            }

            // scaling, if present, is applied to the canvas before text rendered
            canvas.transform(textTransform) {
                text(text, at = textPosition, font = font, color = foregroundColor ?: White)
            }
        }
    }

    inner class OperatorButton(text: String, background: Color = operatorColor, foreground: Color = White, private val method: Double.(Double) -> Double): ToggleButton(text) {
        init {
            configure(this, background, foreground)

            fired += {
                compute()

                activeOperator = this
            }
        }

        internal operator fun invoke(left: Double, right: Double) = method(left, right)
    }

    // endregion

    // region ================ Colors ==================================

    private val numberColor   = Darkgray.darker(0.5f)
    private val operatorColor = Orange.lighter (0.1f)

    // endregion

    // region ================ Internal State ==================================

    private var activeOperator: OperatorButton? = null
        set(new) {
            field = new

            when (field) {
                null -> {
                    divButtton.selected  = false
                    timesButton.selected = false
                    minusButton.selected = false
                    plusButton.selected  = false
                }
            }
        }

    private var reset             = true                    // indicates when to begin a new operand
    private var negated           = false                   // tracks whether number is negative
    private var leftValue         = null as Double?         // left-side operand
    private var rightValue        = null as Double?         // right-side operand
    private var decimalPlace      = 0                       // tracks decimal place for fractions
    private var committedOperator = null as OperatorButton? // operator to be applied to left and right values

    // endregion

    // region ================ Visual Elements =================================

    private val output = Output()

    val result get() = output.number

    val divButtton  = OperatorButton("÷", method = Double::div  )
    val timesButton = OperatorButton("x", method = Double::times)
    val minusButton = OperatorButton("-", method = Double::minus)
    val plusButton  = OperatorButton("+", method = Double::plus )

    val clear  = func("AC" ).apply {
        fired += {
            output.number = 0.0
            clearInternalState()
        }
    }
    val negate = func("+/-").apply {
        fired += {
            if (reset) { output.number = 0.0 }

            reset   = false
            negated = !negated

            output.number *= -1
        }
    }

    val percentButton = func("%").apply {
        fired += {
            output.number *= 0.01
        }
    }
    val decimal = func(".", background = numberColor, foreground = White).apply {
        fired += {
            if (decimalPlace == 0) {
                if (reset) { output.number = 0.0 }

                reset        = false
                output.text += "."   // bit of a hack/short-cut since the number formatter should be used
                decimalPlace = 1
            }
        }
    }

    val equalButton = func("=", operatorColor, White).apply {
        fired += {
            compute()
            clearInternalState()
        }
    }

    @JsName("nine" ) val `9` = number(9)
    @JsName("eight") val `8` = number(8)
    @JsName("seven") val `7` = number(7)
    @JsName("six"  ) val `6` = number(6)
    @JsName("five" ) val `5` = number(5)
    @JsName("four" ) val `4` = number(4)
    @JsName("three") val `3` = number(3)
    @JsName("two"  ) val `2` = number(2)
    @JsName("one"  ) val `1` = number(1)
    @JsName("zero" ) val `0` = number(0)

    // endregion

    // region ================ Button Helpers ==================================

    private fun button(text: String, background: Color = operatorColor, foreground: Color = White) = configure(PushButton(text), background, foreground)
    private fun func  (text: String, background: Color = Lightgray,     foreground: Color = Black) = button(text, background, foreground)
    private fun number(number: Int                                                               ) = button("$number", background = numberColor).apply {
        fired += {
            committedOperator = activeOperator

            val newDigit = (if (negated) -1 else 1) * number.toDouble()

            output.number = when {
                reset             -> number.toDouble()
                decimalPlace == 0 -> output.number * 10 + newDigit
                decimalPlace < 10 -> {
                    val fraction = 1 / 10.0.pow(decimalPlace)

                    (output.number + newDigit * fraction).roundToNearest(fraction).also { ++decimalPlace }
                }
                else              -> output.number
            }

            reset = false
        }
    }

    private fun configure(button: Button, background: Color, foreground: Color) = button.apply {
        size            = Size(60)
        cursor          = Pointer
        behavior        = CalcButtonBehavior(textMetrics)
        foregroundColor = foreground
        backgroundColor = background
    }

    // endregion

    /**
     * Computes the current value based on [leftValue], [rightValue], and [committedOperator].
     */
    private fun compute() {
        when {
            committedOperator != null -> rightValue = output.number
            leftValue        == null  -> leftValue  = output.number
        }

        committedOperator?.let { operator ->
            leftValue?.let { left ->
                rightValue?.let { right ->
                    operator(left, right).also {
                        output.number = it
                        leftValue     = it
                        rightValue    = null
                    }
                    committedOperator = null
                }
            }
        }

        reset        = true
        decimalPlace = 0
    }

    /**
     * Resets all internal state, except [output].
     */
    private fun clearInternalState() {
        reset             = true
        negated           = false
        leftValue         = null
        rightValue        = null
        decimalPlace      = 0
        activeOperator    = null
        committedOperator = null
    }

    /**
     * Updates font for [output] and function buttons using different sizes and weights than the given [font].
     */
    private suspend fun loadFonts() {
        font = fonts("Roboto-Regular.ttf") {
            family = "Roboto"
            weight = 400
            size   = 32
        }

        font?.let { font ->
            fonts("Roboto-Light.ttf") {
                family = "Roboto"
                size   = font.size - 5
                weight = 100
            }?.let { lightFont ->
                output.font        = fonts(lightFont) { size = 72 }
                clear.font         = lightFont
                negate.font        = lightFont
                percentButton.font = lightFont
            }
        }
    }

    init {
        appScope.launch {
            loadFonts()

            ButtonGroup(allowDeselectAll = true, buttons = arrayOf(timesButton, timesButton, minusButton, plusButton))

            val outputHeight  = 100.0
            val buttonSpacing =  10.0

            val gridPanel = GridPanel().apply {
                add(clear, 0, 0); add(negate, 0, 1); add(percentButton, 0, 2); add(divButtton,   0, 3)
                add(`7`,   1, 0); add(`8`,    1, 1); add(`9`,           1, 2); add(timesButton,  1, 3)
                add(`4`,   2, 0); add(`5`,    2, 1); add(`6`,           2, 2); add(minusButton,  2, 3)
                add(`1`,   3, 0); add(`2`,    3, 1); add(`3`,           3, 2); add(plusButton,   3, 3)
                add(`0`,   4, 0,  columnSpan = 2  ); add(decimal,       4, 2); add(equalButton,  4, 3)

                rowSpacing    = { buttonSpacing }
                columnSpacing = { buttonSpacing }
            }

            children += listOf(output, gridPanel)

            // Place output outside grid so the height can be more easily controlled
            val constraints = constrain(output, gridPanel) { output, grid ->
                output.top    eq 0
                output.left   eq 0
                output.right  eq parent.right
                output.height eq outputHeight

                grid.top      eq output.bottom + buttonSpacing
                grid.left     eq output.left
                grid.right    eq output.right
                grid.bottom   eq parent.bottom
            }

            layout = object: Layout by constraints {
                // Set total height to grid panel's ideal width and height, plus output and spacing
                override fun idealSize(container: PositionableContainer, default: Size?) = gridPanel.idealSize?.let {
                    Size(it.width, it.height + outputHeight + buttonSpacing)
                }
            }

            // Force idealSize when gridPanel is laid out
            gridPanel.sizePreferencesChanged += { _,_,new ->
                idealSize = new.idealSize?.let { Size(it.width, it.height + outputHeight + buttonSpacing) }
            }
        }
    }

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, color = Black)
    }
}