import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        browser {
            testTask {
                enabled = false
            }
        }

        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":AnimatingForm"))
            api(project(":Calculator"   ))
            api(project(":Todo"         ))
            api(project(":Photos"       ))
            api(project(":Contacts"     ))
            api(project(":TabStrip"     ))
            api(project(":TimedCards"   ))
        }

        jsMain.dependencies {
            implementation(libs.doodle.browser)
        }
    }
}

setupDocInstall("Development")
setupDocInstall("Production" )

fun setupDocInstall(suffix: String) {
    tasks.register<Copy>("installDocApps$suffix") {
        val webPack = project.tasks.getByName("jsBrowser${suffix}Webpack", KotlinWebpack::class)

        dependsOn(webPack)

        val outputFile   = webPack.mainOutputFile
        val dirToArchive = "$buildDir/../../site/src/components"

        from(outputFile  )
        into(dirToArchive)
    }
}