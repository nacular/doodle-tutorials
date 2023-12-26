package io.nacular.doodle.examples

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.properties.Delegates

/**
 * DynamicListModel of Images that fetches data from Unsplash. This model provides a stream
 * of images by fetching every time the end of its current list is reached. Fetched images
 * are cached in memory.
 */
//sampleStart
class UnSplashDataModel(
    private val scope      : CoroutineScope,
    private val client     : HttpClient,
    private val imageLoader: ImageLoader,
    private val accessToken: String = "YOUR_ACCESS_TOKEN"
): DynamicListModel<Image> {
    @Serializable
    data class Urls(val small: String)

    @Serializable
    data class UnsplashPhoto(val id: String, val urls: Urls)

    // Tracks current HTTP request
    private var httpRequestJob: Job? by Delegates.observable(null) { _, old, _ ->
        old?.cancel()
    }

    // Used to avoid fetching in the middle of an ongoing fetch
    private var fetchActive = false

    // Page being fetched from unsplash
    private var currentPage: Int by observable(-1) { _, _ ->
        fetchActive = true
        httpRequestJob = scope.launch {
            val results = client.get(unsplashLocation).body<List<UnsplashPhoto>>()

            loadedImages.addAll(results.mapNotNull { imageLoader.load(it.urls.small) })

            fetchActive = false

            if (nextPageNeeded) {
                nextPageNeeded = false
                currentPage += 1
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
        it.changed += { _, differences ->
            // notify model observers whenever the underlying list changes (due to image loads)
            changed.forEach {
                it(this, differences)
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

    override fun contains(value: Image) = value in loadedImages
    override fun iterator(                       ) = loadedImages.iterator()
    override fun section (range: ClosedRange<Int>) = loadedImages.subList(range.start, range.endInclusive + 1)
}
//sampleEnd