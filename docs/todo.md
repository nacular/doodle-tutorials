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