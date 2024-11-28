plugins {
    kotlin("multiplatform"          )
    alias(libs.plugins.serialization)
}

kotlin {
    js { browser { binaries.executable() } } // Web (JS) executable

    sourceSets {
        // Web (JS) platform source set
        jsMain.dependencies {
            implementation(libs.coroutines.core    ) // async api calls
            implementation(libs.bundles.ktor.client) // api calls to images service
            implementation(libs.serialization.json ) // serialization for api calls

            implementation(libs.doodle.themes  )
            implementation(libs.doodle.controls)
            implementation(libs.doodle.browser )
        }
    }
}