import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id ("org.jetbrains.kotlin.js")
}

kotlin {
    js {
        browser {
            testTask {
                enabled = false
            }

            dceTask {
                keep("doodle-tutorials-DocApps.calculator")
            }
        }
    }

    dependencies {
        implementation ("org.jetbrains.kotlin:kotlin-stdlib-js")
        implementation (project(":Calculator"))
    }
}

setupDocInstall("Development")
setupDocInstall("Production" )

fun setupDocInstall(suffix: String) {
    tasks.register<Copy>("installDocApps$suffix") {
        val webPack = project.tasks.getByName("browser${suffix}Webpack", KotlinWebpack::class)

        dependsOn(webPack)

        val outputFile  = webPack.outputFile
        val dirToArchive = "$buildDir/../../docs"

        from(outputFile)
        into(dirToArchive)
    }
}
