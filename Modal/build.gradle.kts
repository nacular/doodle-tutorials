plugins {
    kotlin("multiplatform")
}

kotlin {
    jsTargets ()
    jvmTargets()

    val doodleVersion    : String by project
    val coroutinesVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

                api("io.nacular.doodle:core:$doodleVersion"    )
                api("io.nacular.doodle:controls:$doodleVersion")
                api("io.nacular.doodle:themes:$doodleVersion")
                api("io.nacular.doodle:animation:$doodleVersion")
            }
        }
    }
}