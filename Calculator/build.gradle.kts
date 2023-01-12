plugins {
    kotlin("multiplatform")
}

kotlin {
    // Defined in buildSrc/src/main/kotlin/Common.kt
    jsTargets (BOTH)
    jvmTargets()

    // Defined in gradle.properties
    val mockkVersion     : String by project
    val doodleVersion    : String by project
    val coroutinesVersion: String by project

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

                api("io.nacular.doodle:core:$doodleVersion"    ) // required for Doodle
                api("io.nacular.doodle:controls:$doodleVersion") // provides things like buttons and panels
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("io.mockk:mockk:$mockkVersion")
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}