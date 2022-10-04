package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.simpleButtonRenderer
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.text.TextDecoration
import io.nacular.doodle.text.invoke
import io.nacular.doodle.utils.HorizontalAlignment.Left
import kotlin.properties.Delegates.observable

/**
 * A single row in the task list. It has a CheckBox, Label and Button.
 */
class TaskRow(config: TodoConfig, dataStore: DataStore, task: Task): View() {
    /**
     * Tracks the [Task] this row is associated with. This is expected to change
     * as rows are recycled in the [List][io.nacular.doodle.controls.list.List] it is in.
     *
     * Also ensures that [check] is updated when the task changes
     */
    var task: Task by observable(task) { _, _, new ->
        check.selected = new.completed
        when {
            new.completed -> label.styledText = (config.taskCompletedColor) { TextDecoration.LineThrough(new.text) }
            else          -> label.text       = new.text
        }
    }

    // CheckBox that allows marking the underlying [Task] un/complete.
    private val check = CheckBox().apply {
        // mark task when toggled
        selectedChanged += { _,_,_ -> dataStore.mark(this@TaskRow.task, completed = selected) }

        val imageSubRect = Rectangle(6, 0, 34, 40)

        behavior = simpleButtonRenderer { button, canvas ->
            val destination = button.bounds.atOrigin.inset(Insets(left = 13.0, right = 13.0, top = 9.0, bottom = 9.0))

            if (button.selected) canvas.image(config.checkForeground, source = imageSubRect, destination = destination)

            canvas.image(config.checkBackground, source = imageSubRect, destination = destination)
        }
    }

    // Displays the task's text
    private val label = Label(task.text, horizontalAlignment = Left).apply { fitText = emptySet(); foregroundColor = config.labelForeground }

    // Deletion button
    private val delete = PushButton().apply {
        fired    += { dataStore.remove(this@TaskRow.task) } // delete task when clicked
        visible   = false
        behavior  = simpleButtonRenderer { button, canvas ->
            val iconBounds = bounds.atOrigin.inset(Insets(top = 22.0, bottom = 22.0, left = 23.0, right = 23.0))

            val stroke = Stroke(when {
                button.model.pointerOver -> config.deleteHoverColor // TODO: Animate instead
                else                     -> config.deleteColor
            })

            // Draw 2 lines to make X
            canvas.line(iconBounds.position,                   Point(iconBounds.right, iconBounds.bottom), stroke)
            canvas.line(Point(iconBounds.right, iconBounds.y), Point(iconBounds.x,     iconBounds.bottom), stroke)
        }
    }

    init {
        this.task = task
        children += listOf   (check, label, delete)
        layout    = constrain(check, label, delete) { check, label, delete ->
            listOf(check, label, delete).forEach { it.height eq parent.height }
            listOf(check,        delete).forEach { it.width  eq 60.0          }
            check.left   eq 0
            label.left   eq check.right
            label.right  eq delete.left
            (delete.right eq parent.right) .. Strong
        }

        // Used to show/hide the delete button
        pointerChanged += object: PointerListener {
            override fun entered(event: PointerEvent) { delete.visible = true  }
            override fun exited (event: PointerEvent) { delete.visible = false }
        }
    }
}