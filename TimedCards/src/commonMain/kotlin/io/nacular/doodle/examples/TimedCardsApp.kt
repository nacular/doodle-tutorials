package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.invoke
import io.nacular.doodle.animation.transition.easeInOutCubic
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.carousel.Carousel
import io.nacular.doodle.controls.carousel.CarouselBehavior
import io.nacular.doodle.controls.carousel.dampedTransitioner
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.center
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.event.KeyCode.Companion.ArrowLeft
import io.nacular.doodle.event.KeyCode.Companion.ArrowRight
import io.nacular.doodle.event.KeyListener.Companion.pressed
import io.nacular.doodle.event.PointerListener.Companion.on
import io.nacular.doodle.event.PointerMotionListener.Companion.dragged
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.theme.Theme
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.time.Timer
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.times
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

//sampleStart
class TimedCardsApp(
                display     : Display,
                focusManager: FocusManager,
                themeManager: ThemeManager,
                theme       : Theme,
    private val fonts       : FontLoader,
    private val images      : ImageLoader,
    private val timer       : Timer,
    private val animate     : Animator,
    private val textMetrics : TextMetrics,
    private val scheduler   : AnimationScheduler,
                uiDispatcher: CoroutineDispatcher,
): Application {

    private val appScope        = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val itemDefaultSize = Size(CARD_MAX_WIDTH, CARD_MAX_WIDTH * SMALL_CARD_ASPECT)

    private lateinit var appFonts: Fonts

    init {
        appScope.launch(uiDispatcher) {
            themeManager.selected = theme

            appFonts = loadFonts(fonts)

            // Carousel containing all the content
            val carousel = Carousel(
                createModel(images),
                itemVisualizer { item, previous, context ->
                    when (previous) {
                        is Card -> previous.apply { update(item, context) }
                        else    -> Card(item, context, appFonts, itemDefaultSize)
                    }
                }
            ).apply {
                wrapAtEnds = true
                behavior   = object: CarouselBehavior<CardData> {
                    // Presenter controlling which items are shown and how they adjust
                    // as the Carousel moves between frames.
                    override val presenter = CardPresenter<CardData> { itemSize(it) }

                    // Responsible for automatic movement between frames
                    override val transitioner = dampedTransitioner<CardData>(timer, scheduler) { _,_,_, update ->
                        animate(0f to 1f, using = tweenFloat(easeInOutCubic, duration = 1.25 * seconds)) {
                            update(it)
                        }
                    }
                }

                keyChanged += pressed {
                    when (it.code) {
                        ArrowLeft  -> next    () // Move Carousel forward one
                        ArrowRight -> previous() // Move Carousel back one
                    }
                }

                var touchLocation = Origin

                // start/stop manual movement of the Carousel on pointer press/release
                pointerChanged += on(
                    pressed  = {
                        touchLocation = toLocal(it.location, it.target)
                        startManualMove()
                        it.consume()
                    },
                    released = { completeManualMove() },
                )

                // perform manual movement of the Carousel
                pointerMotionChanged += dragged {
                    if (it.source == this) {
                        moveManually(toLocal(it.location, it.target) - touchLocation)
                        it.consume()
                    }
                }
            }

            val buttonControls = ButtonControls(carousel, textMetrics, appFonts)

            display += carousel
            display += buttonControls

            display.layout = constrain(carousel, buttonControls) { carousel_, controls ->
                (carousel_.width  eq CAROUSEL_MAX_WIDTH                  )..Strong
                carousel_.height eq carousel_.width  * MAIN_ASPECT_RATIO
                carousel_.width  eq carousel_.height / MAIN_ASPECT_RATIO

                carousel_.width  lessEq parent.width
                carousel_.height lessEq parent.height
                carousel_.center eq     parent.center

                controls.height eq CONTROLS_HEIGHT
                controls.bottom eq carousel_.bottom
            }.then {
                // done after Carousel's size is properly determined in previous block, otherwise itemSize(carousel.size)
                // won't be accurate since it will use a captured value before the size is updated
                // by the constraint.
                buttonControls.x     = carousel.center.x - itemSize(carousel.size).width / 2
                buttonControls.width = carousel.bounds.right - display.children[1].x
            }

            focusManager.requestFocus(carousel)
        }
    }

    override fun shutdown() {
        appScope.cancel()
    }
}
//sampleEnd