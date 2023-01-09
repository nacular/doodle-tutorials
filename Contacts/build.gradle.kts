plugins {
    kotlin("multiplatform"       )
    kotlin("plugin.serialization")
}

kotlin {
    jsTargets (BOTH)
    jvmTargets()

    val kodeinVersion       : String by project
    val doodleVersion       : String by project
    val coroutinesVersion   : String by project
    val serializationVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                api("org.kodein.di:kodein-di:$kodeinVersion")

                api("io.nacular.doodle:core:$doodleVersion"    )
                api("io.nacular.doodle:controls:$doodleVersion")
                api("io.nacular.doodle:themes:$doodleVersion")
                api("io.nacular.doodle:animation:$doodleVersion")

                api(project(":Modal"))
            }
        }
    }
}