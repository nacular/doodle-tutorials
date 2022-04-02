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
                keep("doodle-tutorials-DocApps.calculator"     )
                keep("doodle-tutorials-DocApps.todo"           )
                keep("doodle-tutorials-DocApps.photos"         )
                keep("doodle-tutorials-DocApps.contacts"       )
                keep("doodle-tutorials-DocApps.contactList"    )
                keep("doodle-tutorials-DocApps.contactView"    )
                keep("doodle-tutorials-DocApps.contactEditing" )
                keep("doodle-tutorials-DocApps.contactCreation")
                keep("doodle-tutorials-DocApps.contactsHeader" )
            }
        }
    }

    val doodleVersion: String by project

    dependencies {
        implementation(project(":Calculator"))
        implementation(project(":Todo"      ))
        implementation(project(":Photos"    ))
        implementation(project(":Contacts"  ))
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