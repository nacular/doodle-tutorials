import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
}

kotlin {
    js {
        browser {
            testTask {
                enabled = false
            }

            dceTask {
                keep("doodle-tutorials-DocApps.calculator")
                keep("doodle-tutorials-DocApps.todo"      )
                keep("doodle-tutorials-DocApps.photos"    )
            }
        }
    }

    dependencies {
        implementation(project(":Calculator"))
        implementation(project(":Todo"      ))
        implementation(project(":Photos"    ))
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
