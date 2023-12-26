plugins {
    kotlin("multiplatform"          )
    alias(libs.plugins.serialization)
}

kotlin {
    // Defined in buildSrc/src/main/kotlin/Common.kt
    jsTargets    ()
    jvmTargets   ()
    wasmJsTargets()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.coroutines.core   )
                api(libs.serialization.json)
                api(libs.kodein.di         )

                api(libs.doodle.controls )
                api(libs.doodle.themes   )
                api(libs.doodle.animation)
            }
        }
    }
}