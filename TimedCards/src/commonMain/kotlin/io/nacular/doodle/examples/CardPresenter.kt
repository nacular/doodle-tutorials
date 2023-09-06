package io.nacular.doodle.examples

import io.nacular.doodle.controls.carousel.Carousel
import io.nacular.doodle.controls.carousel.CarouselBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.geometry.lerp
import io.nacular.doodle.layout.Insets
import kotlin.math.abs
import kotlin.math.min

/**
 * Presenter responsible for displaying the contents of the Carousel
 */
//sampleStart
class CardPresenter<T>(private val spacing: Double = 20.0, private val itemSize: (Size) -> Size): CarouselBehavior.Presenter<T>() {
    /**
     * Determines what is shown in the Carousel
     */
    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
        items            : (at: Position) -> Carousel.PresentedItem?
    ): Presentation {
        var zOrder          = 0
        val results         = mutableListOf<Carousel.PresentedItem>()
        val itemSize        = itemSize(carousel.size)
        var currentBounds   = null as Rectangle?
        var currentPosition = position as Position?
        val mainBounds      = Rectangle(carousel.size).inset(-10.0)

        // Add selected card
        currentPosition?.let(items)?.let {
            // Image is outset by 10, and grows as we progress to the next frame
            it.bounds       = mainBounds.inset(Insets(-progressToNext * itemSize.width * 0.5))
            it.zOrder       = zOrder++
            currentPosition = currentPosition?.next

            results += it
        }

        // Add the first small card
        currentPosition?.let(items)?.let { item ->
            // bounds when exactly at frame
            val bounds = Rectangle(
                Point(
                    (carousel.width - itemSize.width) / 2,
                    (carousel.height - itemSize.height - CONTROLS_HEIGHT)
                ),
                itemSize
            )

            // bounds gradually grows toward main bounds
            item.bounds = lerp(bounds, mainBounds, min(1f, progressToNext * 2))

            // increase item zOrder so they sort properly
            item.zOrder = zOrder++

            // next item is shifted over towards this slot as progress increases
            currentBounds   = bounds.run { at(x - progressToNext * (width + spacing)) }
            currentPosition = currentPosition?.next

            results += item
        }

        // add the remaining cards to the right of the first small one
        do {
            currentPosition?.let(items)?.let { item ->
                // each item is offset to the right of the second, with a spacing that varies w/ progress
                currentBounds = currentBounds?.run {
                    at(right + spacing + -abs(progressToNext - 0.5f) * 3 * spacing + 3 * spacing / 2)
                }

                currentBounds?.let { item.bounds = it }

                // increase item zOrder so they sort properly
                item.zOrder = zOrder++

                results += item
            } ?: break
        } while (
            // continue until there are no more items to show
            (currentBounds?.right ?: 0.0) + spacing < carousel.size.width &&
            currentPosition?.next?.also { currentPosition = it } != null
        )

        return Presentation(items = results)
    }

    override fun distanceToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        items   : (Position) -> Carousel.PresentedItem?
    ): Distance = Distance(Vector2D(x = 1), carousel.width)
}
//sampleEnd