package io.nacular.doodle.examples

import io.nacular.doodle.accessibility.ImageRole
import io.nacular.doodle.controls.carousel.CarouselItem
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.then
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.lerp
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.width
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.utils.Dimension
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.observable
import kotlin.math.min

/**
 * Data objects that are stored in the Carousel's model. These are converted to [Card]s.
 */
data class CardData(val image: Image, val header: String, val title: String, val clip: Rectangle) {
    val width get() = image.width
}

/**
 * Represents each item in the Carousel. These are created by the [ItemVisualizer][io.nacular.doodle.controls.ItemVisualizer]
 * passed to the Carousel.
 */
//sampleStart
class Card(
    private var data           : CardData,
    private var context        : CarouselItem,
    private val fonts          : Fonts,
    private val itemDefaultSize: Size,
): View(accessibilityRole = ImageRole()) {

    private val smallHeader = header(data.header, fonts.smallBoldFont      )
    private val smallTitle  = title(data.title,   fonts.smallBoldFont, -0.8)
    private val largeText   = container {
        +view { render = { rect(bounds.atOrigin, fill = Color.White.paint) } }
        +header(data.header, fonts.largeRegularFont   )
        +title (data.title,  fonts.largeBoldFont, -1.0)

        layout = constrain(children[0], children[1], children[2]) { block, header, title ->
            block.edges eq Rectangle(15, 3)

            header.top  eq block.bottom + 8
            header.left eq block.left
            header.width.preserve
            header.height.preserve

            title.top   eq header.bottom + 7
            title.left  eq 0
            title.right eq parent.right
            title.height.preserve
        }.then {
            this.height = children.last().bounds.bottom
        }

        opacity = 0f
    }

    private var progress by observable(0f) { _, new ->
        largeText.opacity = when {
            new >= 0.5f -> (new - 0.5f) * 2
            else -> 0f
        }

        relayout()
        rerender()
    }

    init {
        size               = data.image.size
        enabled            = false
        clipCanvasToBounds = false

        children += container {
            +view { render = { rect(bounds.atOrigin, fill = Color.White.paint) } }
            +smallHeader
            +smallTitle

            layout = constrain(children[0], smallHeader, smallTitle) { bar, header, title ->
                bar.edges eq Rectangle(7.0, 1.5)

                header.top  eq bar.bottom + 6
                header.left eq bar.left
                header.width.preserve
                header.height.preserve

                title.top   eq header.bottom + 7
                title.left  eq 0
                title.right eq parent.right
                title.height.preserve
            }.then {
                this.height = smallTitle.bounds.bottom
            }
        }
        children += largeText

        layout = constrain(children[0], children[1]) { smallText, largeText ->
            smallText.left   eq parent.centerX - itemDefaultSize.width / 2 + 10
            smallText.right  eq parent.right  - 10
            smallText.bottom eq parent.bottom - 20 + progress * 800
            smallText.height.preserve

            val largeTextOffset = when {
                progress >= 0.5f -> 20 * (1 - (progress - 0.5f) * 2)
                else             -> 0f
            }

            largeText.left  eq  50
            largeText.width eq 500
            largeText.centerY eq parent.centerY + 5 + largeTextOffset
            largeText.height.preserve
        }

        updateProgress()
    }

    /**
     * Called when a Card needs to be recycled to display a new [CardData] or [CarouselItem].
     */
    fun update(data: CardData, context: CarouselItem) {
        this.data    = data
        this.context = context

        updateProgress()
    }

    override fun render(canvas: Canvas) {
        val clip   = lerp(data.clip, Rectangle(data.width, data.width * MAIN_ASPECT_RATIO), min(1f, progress * 2f))
        val radius = 10.0 * (1 - progress * 1.5)

        canvas.outerShadow(blurRadius = 10.0, vertical = 10.0, horizontal = 10.0, color = Black opacity 0.5f) {
            val w = height * clip.width/clip.height

            image(
                image       = data.image,
                source      = clip,
                radius      = radius,
                destination = Rectangle((width - w) / 2, 0.0, w, height)
            )
        }
    }

    private fun updateProgress() {
        progress = when (context.index) {
            context.nearestItem -> 1f
            (context.nearestItem + 1) % context.numItems -> context.progressToNextItem
            else -> 0f
        }
    }

    private fun header(text: String, font: Font) = Label(text).apply {
        this.font     = font
        letterSpacing = 0.0
    }

    private fun title(text: String, font: Font, letterSpacing: Double) = Label(text).apply {
        this.font          = font
        this.letterSpacing = letterSpacing
        wrapsWords         = true
        textAlignment      = TextAlignment.Start
        lineSpacing        = 1.1f
        fitText            = setOf(Dimension.Height)
    }
}
//sampleEnd