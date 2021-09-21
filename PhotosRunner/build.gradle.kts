plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    js().browser()

    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    val doodleVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":Photos"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation ("io.nacular.doodle:browser:$doodleVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation ("io.nacular.doodle:desktop:$doodleVersion")
            }
        }
    }
}

application {
    mainClass.set("io.nacular.doodle.examples.MainKt")
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )