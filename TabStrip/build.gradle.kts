plugins {
    kotlin("multiplatform")
}

kotlin {
    jsTargets (BOTH)
    jvmTargets()

    val doodleVersion: String by project

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("io.nacular.doodle:core:$doodleVersion"     )
                api("io.nacular.doodle:controls:$doodleVersion" )
                api("io.nacular.doodle:animation:$doodleVersion")
            }
        }
    }
}