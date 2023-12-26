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
                api(libs.coroutines.core)
                api(libs.doodle.controls)
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.bundles.test.libs)
            }
        }
    }
}