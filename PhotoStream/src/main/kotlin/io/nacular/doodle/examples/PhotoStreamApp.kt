package io.nacular.doodle.examples

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.list.DynamicList
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.height
import io.nacular.doodle.image.width
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.fill
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.basicVerticalListBehavior
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.min
import kotlin.properties.Delegates.observable

/**
 * DynamicListModel of Images that fetches data from Unsplash. This model provides a stream
 * of images by fetching every time the end of its current list is reached. Fetched images
 * are cached in memory.
 */
class UnSplashDataModel(
        private val scope      : CoroutineScope,
        httpClient             : HttpClient,
        private val imageLoader: ImageLoader,
        private val accessToken: String = "YOUR_ACCESS_TOKEN"
): DynamicListModel<Image> {
    @Serializable
    data class Urls(val small: String)

    @Serializable
    data class UnsplashPhoto(val id: String, val urls: Urls)

    // HTTP client configured to read JSON  returned from unsplash
    private val client = httpClient.config {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json { ignoreUnknownKeys = true })
        }
    }

    // Tracks current HTTP request
    private var httpRequestJob: Job? by observable(null) { _,old,_ ->
        old?.cancel()
    }

    // Used to avoid fetching in the middle of an ongoing fetch
    private var fetchActive = false

    // Page being fetched from unsplash
    private var currentPage: Int by observable(-1) { _,_ ->
        fetchActive    = true
        httpRequestJob = scope.launch {
            val results = client.get<List<UnsplashPhoto>>(unsplashLocation)

            loadedImages.addAll(results.mapNotNull { imageLoader.load(it.urls.small) })

            fetchActive = false

            if (nextPageNeeded) {
                nextPageNeeded  = false
                currentPage    += 1
            }
        }
    }

    private val pageSize               = 5
    private var nextPageNeeded         = false
    private val unsplashLocation get() = "https://api.unsplash.com/photos/?client_id=$accessToken&page=$currentPage&per_page=$pageSize"

    override val size get() = loadedImages.size

    override val changed = SetPool<ModelObserver<Image>>()

    // Internal list used to cache images loaded from unsplash
    private val loadedImages = ObservableList<Image>().also {
        it.changed += { _ ,removed, added, moved ->
            // notify model observers whenever the underlying list changes (due to image loads)
            changed.forEach {
                it(this, removed, added, moved)
            }
        }
    }

    init {
        currentPage = 0
    }

    override fun get(index: Int): Result<Image> = Result.runCatching {
        loadedImages[index].also {
            // Load the next page if the last image is fetched from the model
            if (index == size - 1) {
                when {
                    fetchActive -> nextPageNeeded = true
                    else        -> currentPage += 1
                }
            }
        }
    }

    override fun contains(value: Image           ) = value in loadedImages
    override fun iterator(                       ) = loadedImages.iterator()
    override fun section (range: ClosedRange<Int>) = loadedImages.subList(range.start, range.endInclusive + 1)
}

/**
 * Renders an image with a center crop.
 */
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

/**
 * Streams an unbounded list of images from unsplash and displays them in a list.
 */
class PhotoStreamApp(display    : Display,
                     themes     : ThemeManager,
                     theme      : DynamicTheme,
                     httpClient : HttpClient,
                     imageLoader: ImageLoader): Application {
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
            contentWidthConstraints = { parent.width - parent.scrollBarWidth }
        }

        display.layout = constrain(display.children[0]) {
            it.width   eq min(parent.width, imageHeight)
            it.height  eq parent.height
            it.centerX eq parent.centerX
        }
    }

    override fun shutdown() {}
}