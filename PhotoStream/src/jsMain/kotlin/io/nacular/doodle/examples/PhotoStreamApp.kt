package io.nacular.doodle.examples

import io.ktor.client.*
import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.list.DynamicList
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Display
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.basicVerticalListBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Streams an unbounded list of images from unsplash and displays them in a list.
 */
//sampleStart
class PhotoStreamApp(
    display    : Display,
    themes     : ThemeManager,
    theme      : DynamicTheme,
    httpClient : HttpClient,
    imageLoader: ImageLoader
): Application {
    init {
        // For scroll panel behavior
        themes.selected = theme

        val appScope    = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val imageHeight = 400.0

        // List to hold images
        val list = DynamicList(
            model          = UnSplashDataModel(appScope, httpClient, imageLoader),
            itemVisualizer = itemVisualizer { image, recycledView, _ -> when(recycledView) {
                is CenterCroppedPhoto -> recycledView.also { recycledView.image = image }
                else                  -> CenterCroppedPhoto(image)
            } }
        ).apply {
            behavior      = basicVerticalListBehavior(itemHeight = imageHeight)
            cellAlignment = fill
        }

        display += ScrollPanel(list).apply {
            // Ensure list's width is equal to scroll-panel's
            contentWidthConstraints = { it eq parent.width - verticalScrollBarWidth }
        }

        display.layout = constrain(display.children[0]) {
            it.width   eq     parent.width strength Strong
            it.width   lessEq imageHeight
            it.height  eq     parent.height
            it.centerX eq     parent.centerX
        }
    }

    override fun shutdown() {}
}
//sampleEnd