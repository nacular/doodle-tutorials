plugins {
    kotlin("multiplatform")
}

kotlin {
    // Defined in buildSrc/src/main/kotlin/Common.kt
    jsTargets    ()
    jvmTargets   ()
    wasmJsTargets()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.doodle.animation)
            }
        }
    }
}