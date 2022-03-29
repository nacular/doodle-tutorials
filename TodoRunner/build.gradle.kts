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
                implementation(project(":Todo"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation ("io.nacular.doodle:browser:$doodleVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                val osName = System.getProperty("os.name")
                val targetOs = when {
                    osName == "Mac OS X"       -> "macos"
                    osName.startsWith("Win"  ) -> "windows"
                    osName.startsWith("Linux") -> "linux"
                    else                       -> error("Unsupported OS: $osName")
                }

                val osArch = System.getProperty("os.arch")
                val targetArch = when (osArch) {
                    "x86_64", "amd64" -> "x64"
                    "aarch64"         -> "arm64"
                    else              -> error("Unsupported arch: $osArch")
                }

                val target = "${targetOs}-${targetArch}"

                implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion")
            }
        }
    }
}

application {
    mainClass.set("MainKt")
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )