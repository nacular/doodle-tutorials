# [Todo](https://github.com/nacular/doodle-tutorials/tree/master/Todo) Tutorial
----


```doodle
{
    "border": false,
    "height": "700px",
    "run"   : "DocApps.todo"
}
```

?> You can also see the full-screen app [here](https://nacular.github.io/doodle-tutorials/todo).

--- 

## Project Setup

We will use a multi-platform setup for this app. This is not necessary to use Doodle, but it lets us implement our code almost
entirely in commonMain and our tests in commonTest. We will then be able to run these tests on the JVM target, which will
make their execution really fast and completely decoupled from the JS environment.

[**build.gradle.kts**](https://github.com/nacular/doodle-tutorials/blob/master/Todo/build.gradle.kts)

```kotlin
plugins {
    kotlin("multiplatform")
}

kotlin {
    // Defined in buildSrc/src/main/kotlin/Common.kt
    jsTargets ()
    jvmTargets()

    // Defined in gradle.properties
    val mockkVersion        : String by project
    val doodleVersion       : String by project
    val mockkJsVersion      : String by project
    val coroutinesVersion   : String by project
    val serializationVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion") // async font, image loading
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion" ) // task persistence

                api ("io.nacular.doodle:core:$doodleVersion"    ) // required for Doodle
                api ("io.nacular.doodle:browser:$doodleVersion" ) // required for Doodle in the browser
                api ("io.nacular.doodle:controls:$doodleVersion") // provides things like buttons and panels
            }
        }

       // ...
    }
}
```

---

## Defining Our Application

All Doodle apps must implement the [`Application`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/application/Application.kt#L4)
interface. The framework will then initialize our app via the constructor. Our app will be fairly simple: just create
an instance of our calculator and add it to the display.

Doodle apps can be defined in `commonMain`, since they do not require any platform-specific dependencies. Therefore, we will do
the same and place ours in `commonMain/kotlin/io/nacular/doodle/examples`.

[**TodoApp.kt**](https://github.com/nacular/doodle-tutorials/blob/master/Todo/src/commonMain/kotlin/io/nacular/doodle/examples/TodoApp.kt#L11)

```kotlin
class TodoApp(display: Display, /*...*/): Application {
    init {
        // Launch coroutine to fetch fonts/images
        GlobalScope.launch {
            val titleFont  = fonts            { family = "Helvetica Neue"; size = 100; weight = 100 }
            val listFont   = fonts(titleFont) {                            size =  24               }
            val footerFont = fonts(titleFont) {                            size =  10               }

            // install theme
            themes.selected = theme

            display += TodoView(/*...*/)

            display.layout = constrain(display.children[0]) { fill(it) }

            display.fill(config.appBackground.paint)
        }
    }

    override fun shutdown() { /* no-op */ }
}
```

?> Notice that `shutdown` is a no-op, since we don't have any cleanup to do when the app closes.

---

## Defining Main + Fullscreen

Doodle apps can be [launched](https://nacular.github.io/doodle/#/applications) in a few different ways.
For our purposes, we will create a `main` and run this [top-level](https://nacular.github.io/doodle/#/applications?id=top-level-apps) in full screen.

[**Main.kt**](https://github.com/nacular/doodle-tutorials/blob/master/Todo/src/jsMain/kotlin/io/nacular/doodle/examples/Main.kt#L12)

```kotlin
package io.nacular.doodle.examples

//...

fun main() {
    application(modules = listOf(FontModule, PointerModule, KeyboardModule, basicLabelBehavior(),
        nativeTextFieldBehavior(), nativeHyperLinkBehavior(), nativeScrollPanelBehavior(smoothScrolling = true),
        Module(name = "AppModule") {
            bind<ImageLoader>         () with singleton { ImageLoaderImpl         (instance(), instance()            ) }
            bind<PersistentStore>     () with singleton { LocalStorePersistence   (                                  ) }
            bind<NativeLinkStyler>    () with singleton { NativeLinkStylerImpl    (instance()                        ) }
            bind<DataStore>           () with singleton { DataStore               (instance()                        ) }
            bind<Router>              () with singleton { TrivialRouter           (window                            ) }
            bind<FilterButtonProvider>() with singleton { LinkFilterButtonProvider(instance(), instance(), instance()) }
        }
    )) {
        // load app
        TodoApp(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}
```

The `application` function is used to launch top-level apps. It takes a list of modules to include and a lambda that builds the
app being launched. This lambda is within a Kodein injection context, which means we can inject dependencies into our app via
`instance`, `provider`, etc.

Notice that we have included several modules for our app. This includes one for fonts, pointer, keyboard, and several for various
View [`Behaviors`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Behavior.kt#L7) 
(i.e. [`nativeTextFieldBehavior()`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/theme/native/NativeTheme.kt#L108)) 
which loads the native behavior for TextFields. We also define some bindings
directly in a new module. These are items with no built-in module, or items that only exist in our app code. 

?> Check out Kodein to learn more about how it handles dependency injection.

The `application` function also takes an optional HTML element within which the app will be hosted. The app will be hosted in
`document.body` if no element is specified.

App launching is the only part of our code that is platform-specific. This makes sense, since this is the only time we might care
about an HTML element. And the reason is to support use-cases where apps are embedded into non-Doodle contexts.
