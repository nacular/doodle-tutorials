package io.nacular.doodle.examples

import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.ImageLoader

const val CONTROLS_HEIGHT    =  100.0
const val SMALL_CARD_ASPECT  =  886.0 / 575
const val MAIN_ASPECT_RATIO  =    9.0 /  16
const val CAROUSEL_MAX_WIDTH = 1094.0
const val CARD_MAX_WIDTH     =  161.0

data class Fonts(
    val smallRegularFont: Font,
    val smallBoldFont   : Font,
    val mediumBoldFont  : Font,
    val largeRegularFont: Font,
    val largeBoldFont   : Font
)

/**
 * Size of small cards in the carousel
 */
fun itemSize(carouselSize: Size): Size {
    val itemWidth = CARD_MAX_WIDTH * carouselSize.width / CAROUSEL_MAX_WIDTH

    return Size(itemWidth, itemWidth * SMALL_CARD_ASPECT)
}

/**
 * Load fonts needed for the app
 */
suspend fun loadFonts(fonts: FontLoader): Fonts {
    val smallRegularFont = fonts("Oswald-Light.ttf") {
        family = "Oswald"
        weight = 100
        size   = 10
    }!!

    val smallBoldFont = fonts("Oswald.ttf") {
        family = "Oswald"
        weight = 400
        size   = 16
    }!!

    val largeBoldFont    = fonts(smallBoldFont   ) { size = 60 }!!
    val mediumBoldFont   = fonts(smallBoldFont   ) { size = 30 }!!
    val largeRegularFont = fonts(smallRegularFont) { size = 16 }!!

    return Fonts(
        smallBoldFont    = smallBoldFont,
        largeBoldFont    = largeBoldFont,
        mediumBoldFont   = mediumBoldFont,
        smallRegularFont = smallRegularFont,
        largeRegularFont = largeRegularFont
    )
}

/**
 * Creates the model that stores data for the Carousel
 */
suspend fun createModel(images: ImageLoader) = SimpleListModel(listOf(
    listOf("biker",    Rectangle(162,   0, 784, 1208), "Switzerland Alps",              "SAINT\nANTÖNIEN"        ),
    listOf("monkey",   Rectangle(190,   0, 575,  886), "Japan Alps",                    "NAGANO\nPREFECTURE"     ),
    listOf("dunes",    Rectangle(316,   0, 298,  459), "Sahara Desert - Morocco",       "MARRAKECH\nMERZOUGA"    ),
    listOf("yosemite", Rectangle(107,   0, 360,  555), "Sierra Nevada - United States", "YOSEMITE\nNATIONAL PARK"),
    listOf("surfer",   Rectangle(745,   0, 778, 1199), "Tanifa - Spain",                "LOS LANCES\nBEACH"      ),
    listOf("balloons", Rectangle(892, 148, 769, 1185), "Cappadocia - Turkey",           "GÖREME\nVALLEY"         ),
).map { (name, clip, header, title) ->
    CardData(images.load("$name.png")!!, header as String, title as String, clip as Rectangle)
})
