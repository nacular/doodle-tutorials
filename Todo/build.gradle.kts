plugins {
    kotlin("multiplatform"          )
    alias(libs.plugins.serialization)
}

kotlin {
    // Defined in buildSrc/src/main/kotlin/Common.kt
    jsTargets    ()
    wasmJsTargets()
    jvmTargets   ()

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core   )
            api(libs.serialization.json)

            api(libs.doodle.themes     )
            api(libs.doodle.controls   )
        }

        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.bundles.test.libs)
        }
    }
}