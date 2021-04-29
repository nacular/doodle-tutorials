# [Photos](https://github.com/nacular/doodle-tutorials/tree/master/Photos) Tutorial
----

We will build a simple photo app that lets you view and manipulate images using a pointer or multi-touch. Images will be added to the app via drag-drop.
You can then move, size, and rotate them with a mouse, pointer, touch, or via an info overlay.

Here is the end result.

```doodle
{
    "border": false,
    "height": "700px",
    "run"   : "DocApps.photos"
}
```

?> You can also see the full-screen app [here](https://nacular.github.io/doodle-tutorials/photos).

--- 

## Project Setup

We will use a JS only-setup for this app.

[**build.gradle.kts**](https://github.com/nacular/doodle-tutorials/blob/master/Photos/build.gradle.kts)

```kotlin
plugins {
    kotlin("js")
}

kotlin {
    jsTargets()

    // Defined in gradle.properties
    val doodleVersion    : String by project
    val coroutinesVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion") // async image loading
        
                implementation("io.nacular.doodle:core:$doodleVersion"     ) // required for Doodle
                implementation("io.nacular.doodle:browser:$doodleVersion"  ) // required for Doodle in the browser
                implementation("io.nacular.doodle:controls:$doodleVersion" ) // provides things like buttons and panels
                implementation("io.nacular.doodle:animation:$doodleVersion") // animations
                implementation("io.nacular.doodle:themes:$doodleVersion"   ) // for Basic theme
            }
        }
    }
}
```

---

## Defining Our Application

All Doodle apps must implement the [`Application`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/application/Application.kt#L4)
interface. The framework will then initialize our app via the constructor.

The app's structure is fairly simple. It has a main Container that holds the images and supports drag-drop, and a panel with controls for manipulating
a selected image.

[**PhotosApp.kt**](https://github.com/nacular/doodle-tutorials/blob/master/PHotos/src/commonMain/kotlin/io/nacular/doodle/examples/PhotosApp.kt#L11)

```kotlin
class PhotosApp(/*...*/): Application {
    init {
        // ...

        val panelToggle // Button used to show/hide the panel
        val panel       // Has controls for manipulating images

        
        val mainContainer = container {
            // ...
            
            dropReceiver = object: DropReceiver {
                // support drag-drop importing
            }

            GlobalScope.launch {
                listOf("tetons.jpg", "earth.jpg").forEachIndexed { index, file ->
                    // load default images
                }
            }
        }

        display += listOf(mainContainer, panel, panelToggle)

        // ...
    }
    
    override fun shutdown() {}
}
```

?> Notice that `shutdown` is a no-op, since we don't have any cleanup to do when the app closes.

---

## Defining Main + Fullscreen

Doodle apps can be [launched](https://nacular.github.io/doodle/#/applications) in a few different ways.
For our purposes, we will create a `main` and run this [top-level](https://nacular.github.io/doodle/#/applications?id=top-level-apps) in full screen.

[**Main.kt**](https://github.com/nacular/doodle-tutorials/blob/master/Photos/src/main/kotlin/io/nacular/doodle/examples/Main.kt#L22)

```kotlin
package io.nacular.doodle.examples

//...

fun main() {
    application(modules = listOf(
        FocusModule,
        KeyboardModule,
        DragDropModule,
        basicLabelBehavior(),
        nativeTextFieldBehavior(spellCheck = false),
        basicMutableSpinnerBehavior(),
        basicCircularProgressIndicatorBehavior(thickness = 18.0),
        Module(name = "AppModule") {
            bindSingleton<Animator>    { AnimatorImpl   (instance(), instance()) }
            bindSingleton<ImageLoader> { ImageLoaderImpl(instance(), instance()) }
        }
    )) {
        // load app
        PhotosApp(instance(), instance(), instance(), instance(), instance(), instance())
    }
}
```

Use the `application` function to launch top-level apps. It takes a list of modules, and a lambda that builds the
app. This lambda is within a Kodein injection context, which means we can inject dependencies into our app via
`instance`, `provider`, etc.

Notice that we have included several modules for our app. This includes one for focus, keyboard, drag-drop and several for various
View [`Behaviors`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Behavior.kt#L7) 
(i.e. [`nativeTextFieldBehavior()`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/theme/native/NativeTheme.kt#L108)) 
which loads the native behavior for TextFields. We also define some bindings directly in a new module. These are items with no
built-in module, or items that only exist in our app code. 

?> Check out Kodein to learn more about how it handles dependency injection.

The `application` function also takes an optional HTML element within which the app will be hosted. The app will be hosted in
`document.body` if you do not specify an element.

App launching is the only part of our code that is platform-specific; since it is the only time we might care
about an HTML element. It also helps support embedding apps into non-Doodle contexts.

## Drag-drop Support

Drag-drop support requires the DragDropModule to work. It then requires setting up drag/drop recognizers on the source/target Views. This app creates
a main Container for this.

```kotlin
class PhotosApp(/*...*/ private val images: ImageLoader /*...*/): Application {
    init {
        // ...
        val mainContainer = container {
            // ...
                    
            dropReceiver = object: DropReceiver {
                private  val allowedFileTypes                    = Files(ImageType("jpg"), ImageType("jpeg"), ImageType("png"))
                override val active                              = true
                private  fun allowed          (event: DropEvent) = allowedFileTypes in event.bundle
                override fun dropEnter        (event: DropEvent) = allowed(event)
                override fun dropOver         (event: DropEvent) = allowed(event)
                override fun dropActionChanged(event: DropEvent) = allowed(event)
                override fun drop             (event: DropEvent) = event.bundle[allowedFileTypes]?.let { files ->
                    val photos = files.map { GlobalScope.async { images.load(it)?.let { FixedAspectPhoto(it) } } }
        
                    GlobalScope.launch {
                        photos.mapNotNull { it.await() }.forEach { photo ->
                            import(photo, event.location)
                        }
                    }
                    true
                } ?: false
            }
        }
    }

    // ...
}
```

The DropReceiver specifies the supported file-types. It then checks that any drop event contains valid files before accepting it. The 
`drop(event: DropEvent)` method is called when the user attempts the final drop. Here, the receiver fetches all the allowed files in the bundle,
and tries to load and import each one. Notice that the receiver converts raw Image returned by `ImageLoader` into a `FixedAspectPhoto`.