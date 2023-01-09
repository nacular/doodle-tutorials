package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.theme.simpleTextButtonRenderer
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.text
import io.nacular.doodle.event.KeyCode
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor
import io.nacular.measured.units.Angle
import io.nacular.measured.units.times

/**
 * This is the text-input box with select all. It contains a Button and a TextField.
 */
class TaskCreationBox(private val focusManager: FocusManager, textMetrics: TextMetrics, config: TodoConfig, dataStore: DataStore): View() {
    init {
        cursor    = Cursor.Text
        height    = 65.0
        children += PushButton("❯").apply {
            font     = config.listFont
            width    = 60.0
            fired   += { dataStore.markAll(completed = dataStore.active.isNotEmpty()) } // toggle all when pressed
            cursor   = Cursor.Default
            visible  = !dataStore.isEmpty
            behavior = simpleTextButtonRenderer(textMetrics) { button, canvas ->
                // Rotate the text by 90°
                canvas.rotate(around = Point(button.width/2, button.height/2), by = 90 * Angle.degrees) {
                    text(button.text, font(button), textPosition(button), if (dataStore.active.isEmpty()) config.selectAllColor else config.placeHolderColor)
                }
            }

            dataStore.changed += {
                visible = !it.isEmpty // update toggle visibility when tasks change
                rerender()
            }
        }

        children += TextField().apply {
            font              = config.listFont
            placeHolder       = config.placeHolderText
            borderVisible     = false
            foregroundColor   = config.labelForeground
            placeHolderFont   = config.placeHolderFont
            placeHolderColor  = config.placeHolderColor
            backgroundColor   = config.textFieldBackground
            keyChanged       += KeyListener.released { event ->
                if (event.code == KeyCode.Enter && text.isNotBlank()) {
                    dataStore.add(Task(text.trim()))
                    text = ""
                }
            }
        }

        layout = constrain(children[0], children[1]) { button, textField ->
            listOf(button, textField).forEach { it.height eq parent.height }
            button.left     eq 0
            button.width.preserve
            textField.left   eq button.right
            (textField.right eq parent.right) .. Strong
        }

        // Ensure TextField has focus when bar is clicked (happens if toggle invisible)
        pointerChanged += PointerListener.released { focusManager.requestFocus(children[1]) }
    }

    /**
     * This is called whenever the View is added to the [Display]. This is a good time to
     * request focus for the TextField.
     */
    override fun addedToDisplay() { focusManager.requestFocus(children[1]) }
}