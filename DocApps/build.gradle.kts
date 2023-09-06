import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
}

kotlin {
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }

        binaries.executable()
    }

    val doodleVersion: String by project

    dependencies {
        implementation(project(":Calculator"))
        implementation(project(":Todo"      ))
        implementation(project(":Photos"    ))
        implementation(project(":Contacts"  ))
        implementation(project(":TabStrip"  ))
        implementation(project(":TimedCards"))
        implementation("io.nacular.doodle:browser:$doodleVersion")
    }
}

setupDocInstall("Development")
setupDocInstall("Production" )

fun setupDocInstall(suffix: String) {
    tasks.register<Copy>("installDocApps$suffix") {
        val webPack = project.tasks.getByName("browser${suffix}Webpack", KotlinWebpack::class)

        dependsOn(webPack)

        val outputFile   = webPack.outputFile
        val dirToArchive = "$buildDir/../../site/src/components" //"$buildDir/../../docs"

        from(outputFile  )
        into(dirToArchive)
    }
}