package io.nacular.doodle.examples

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.height
import io.nacular.doodle.image.width
import kotlin.math.min

/**
 * Renders an image with a center crop.
 */
//sampleStart
class CenterCroppedPhoto(image: Image): View() {
    private lateinit var centerCrop: Rectangle

    var image: Image = image
        set(new) {
            field        = new
            val cropSize = min(image.width, image.height)
            centerCrop   = Rectangle((image.width - cropSize) / 2, (image.height - cropSize) / 2, cropSize, cropSize)

            rerender()
        }

    init {
        this.image = image // ensure setter called, so centerCrop initialized
    }

    override fun render(canvas: Canvas) {
        canvas.image(image, source = centerCrop, destination = bounds.atOrigin)
    }
}
//sampleEnd