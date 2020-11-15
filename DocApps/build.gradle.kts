import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id ("org.jetbrains.kotlin.js")
}

kotlin {
    kotlin.target.browser {
        testTask {
            enabled = false
        }

        dceTask {
            keep("doodle-tutorials-DocApps.calculator")
        }
    }

    dependencies {
        implementation ("org.jetbrains.kotlin:kotlin-stdlib-js")
        implementation (project(":Calculator"))
    }
}

tasks.register<Copy>("copyOutput") {
    val outputFile  = project.tasks.getByName("browserProductionWebpack", KotlinWebpack::class).outputFile
    val dirToArchive = "$buildDir/../../docs"

    from(outputFile)
    into(dirToArchive)
}