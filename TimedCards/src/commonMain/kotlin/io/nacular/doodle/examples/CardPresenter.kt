package io.nacular.doodle.examples

import io.nacular.doodle.controls.carousel.Carousel
import io.nacular.doodle.controls.carousel.CarouselBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.geometry.at
import io.nacular.doodle.geometry.inset
import io.nacular.doodle.geometry.lerp
import io.nacular.doodle.layout.Insets
import kotlin.math.abs
import kotlin.math.min

/**
 * Presenter responsible for displaying the contents of the Carousel
 */
//sampleStart
class CardPresenter<T>(private val spacing: Double = 20.0, private val itemSize: (Size) -> Size): CarouselBehavior.Presenter<T>() {
    private val maxSpacing = spacing * 3

    /**
     * Determines what is shown in the Carousel
     */
    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
    ): Presentation {
        var zOrder          = 0
        val results         = mutableListOf<Carousel.PresentedItem>()
        val itemSize        = itemSize(carousel.size)
        var currentBounds   = null as Rectangle?
        var currentPosition = position as Position?
        val mainBounds      = Rectangle(carousel.size).inset(-10.0)

        // Add selected card
        carousel.item(at = currentPosition)?.let {
            // Image is outset by 10, and grows as we progress to the next frame
            it.bounds       = mainBounds.inset(Insets(-progressToNext * itemSize.width * 0.5))
            it.zOrder       = zOrder++
            currentPosition = currentPosition?.next

            results += it
        }

        // Add the first small card
        carousel.item(at = currentPosition)?.let { item ->
            // bounds when exactly at frame
            currentBounds = Rectangle(
                x      = (carousel.width  - itemSize.width) / 2,
                y      = (carousel.height - itemSize.height - CONTROLS_HEIGHT),
                width  = itemSize.width,
                height = itemSize.height
            )

            // bounds gradually grows toward main bounds
            item.bounds = lerp(currentBounds, mainBounds, min(1f, progressToNext * 2))

            // increase item zOrder so they sort properly
            item.zOrder = zOrder++

            // next item is shifted over towards this slot as progress increases
            currentBounds = currentBounds.run { at(x - progressToNext * (width + spacing)) }

            results += item
        }

        // add the remaining cards to the right of the first small one
        results += currentPosition?.allNext?.asSequence()?.mapNotNull { carousel.item(it)?.apply {
            // each item is offset to the right of the second, with a spacing that varies w/ progress
            currentBounds = currentBounds!!.run {
                at(right + spacing + -abs(progressToNext - 0.5f) * maxSpacing + maxSpacing / 2)
            }

            this.bounds = currentBounds

            // increase item zOrder so they sort properly
            this.zOrder = zOrder++
        } }?.takeWhile {
            currentBounds!!.x < carousel.width
        }?.toList() ?: emptyList()

        return Presentation(items = results)
    }

    override fun distanceToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
    ): Distance = Distance(Vector2D(x = 1), carousel.width)
}
//sampleEnd