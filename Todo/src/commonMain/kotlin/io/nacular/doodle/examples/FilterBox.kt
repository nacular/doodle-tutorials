package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.examples.DataStore.Filter.Active
import io.nacular.doodle.examples.DataStore.Filter.Completed
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.text.TextDecoration.Companion.UnderLine
import io.nacular.doodle.text.invoke

/**
 * The set of controls below the task list that shows # of remaining tasks,
 * filter and clear-all buttons.
 */
class FilterBox(private val config              : TodoConfig,
                private val dataStore           : DataStore,
                private val textMetrics         : TextMetrics,
                private val filterButtonProvider: FilterButtonProvider): View() {

    /**
     * Behavior to style the toggle buttons.
     */
    private fun filterButtonBehavior(widthInset : Double = 16.0,
                                     renderBlock: CommonTextButtonBehavior<Button>.(Button, Canvas) -> Unit
    ): Behavior<Button> = object: CommonTextButtonBehavior<Button>(textMetrics) {
        override fun install(view: Button) {
            super.install(view)

            // Pad each button
            view.size = textMetrics.size(view.text, config.filterFont).run { Size(width + widthInset, height + 8.0) }
        }

        override fun render(view: Button, canvas: Canvas) {
            renderBlock(this, view, canvas)

            // Draw text
            view.foregroundColor?.paint?.let { canvas.text(view.text, font(view), textPosition(view), it) }
        }
    }

    private fun filterButton(text: String, filter: DataStore.Filter? = null): Button {
        // Get new filter button from the provider, giving it a [Behavior] to install
        return filterButtonProvider(text, filter, filterButtonBehavior { button, canvas ->
            val selected = button.model.selected || dataStore.filter == filter

            if (button.model.pointerOver || selected) {
                canvas.rect(button.bounds.atOrigin.inset(0.5), radius = 3.0, stroke = Stroke(when {
                    selected -> config.headerColor opacity 0.2f
                    else     -> config.headerColor opacity 0.1f
                }))
            }
        }).apply {
            font            = config.filterFont
            foregroundColor = config.filterButtonForeground
        }
    }

    private val itemsLeft = Label().apply { foregroundColor = config.labelForeground }
    private val clearAll  = PushButton(config.clearCompletedText).apply {
        behavior = filterButtonBehavior(widthInset = 30.0) { view, canvas ->
            val color = config.filterButtonForeground
            val font  = font(view)
            when {
                // Create styled text by nesting decoration, color, and font
                model.pointerOver -> canvas.text(UnderLine { color { font(view.text) } }, textPosition(view))
                else              -> canvas.text(view.text, font, textPosition(view), color.paint)
            }
        }

        fired += { dataStore.removeCompleted() }
    }

    // Sync with data store changes
    private fun update() {
        visible          = !dataStore.isEmpty
        itemsLeft.text   = dataStore.active.size.let { active -> "$active ${if (active > 1) "Items" else "Item"} left" }
        clearAll.visible = dataStore.completed.isNotEmpty()
    }

    init {
        update()
        val all       = filterButton("All"                 )
        val active    = filterButton("Active",    Active   )
        val completed = filterButton("Completed", Completed)
        dataStore.changed += { update() }
        font      = config.filterFont
        height    = 41.0
        children += listOf(itemsLeft, all, active, completed, clearAll)
        layout    = constrain(itemsLeft, all, active, completed, clearAll) { label, all_, active_, completed_, clearAll ->
            listOf(label, all_, active_, completed_, clearAll).forEach { it.centerY = parent.centerY }
            val spacing     = 6.0
            label.left      = parent.left  + 15
            clearAll.right  = parent.right
            clearAll.height = parent.height
            all_.left       = parent.left + (parent.width - { all.width + active.width + completed.width + spacing * 2 }) / 2
            active_.left    = all_.right    + spacing
            completed_.left = active_.right + spacing
        }
    }

    override fun render(canvas: Canvas) {
        // Horizontal line at top of filter box
        canvas.line(Point(y = 0.5), Point(width, 0.5), Stroke(config.lineColor))
    }
}