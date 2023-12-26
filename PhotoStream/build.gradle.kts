plugins {
    kotlin("multiplatform"          )
    alias(libs.plugins.serialization)
}

kotlin {
    // Defined in buildSrc/src/main/kotlin/Common.kt
    jsTargets    (executable = true)
    wasmJsTargets(executable = true)

    sourceSets {
        jsMain {
            dependencies {
                implementation(libs.bundles.ktor.client)
                implementation(libs.coroutines.core    )
                implementation(libs.serialization.json )

                api(libs.doodle.browser )
                api(libs.doodle.controls)
                api(libs.doodle.themes  )
            }
        }
    }
}