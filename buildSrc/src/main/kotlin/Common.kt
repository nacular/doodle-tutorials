import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.jsTargets() {
    js {
        compilations.all {
            kotlinOptions {
                moduleKind = "umd"
                sourceMapEmbedSources = "always"
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
            }
        }
        browser {
            testTask {
                enabled = false
            }
        }
    }
}

fun KotlinMultiplatformExtension.jvmTargets() {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
            }
        }
    }
}

fun Project.installFullScreenDemo(suffix: String) {
    tasks.register<Copy>("installFullScreenDemo$suffix") {
        val webPack = project.tasks.getByName("jsBrowser${suffix}Webpack", org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class)

        dependsOn(webPack)

        val kotlinExtension = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
        val kotlinSourceSets = kotlinExtension.sourceSets

        val jsFile       = webPack.outputFile
        val htmlFile     = kotlinSourceSets.getByName("jsMain").resources.single { it.name == "index.html" }
        val docDirectory = "$buildDir/../../docs/${project.name.toLowerCase()}"

        from(htmlFile, jsFile)
        into(docDirectory)
    }
}