plugins {
    kotlin("multiplatform")
}

kotlin {
    jsTargets (BOTH)
    jvmTargets()

    val mockkVersion     : String by project
    val doodleVersion    : String by project
    val coroutinesVersion: String by project

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

                api("io.nacular.doodle:core:$doodleVersion"    )
                api("io.nacular.doodle:controls:$doodleVersion")
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