import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithJsPresetFunctions
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

private fun KotlinJsTargetDsl.configure() {
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

fun KotlinJsProjectExtension.jsTargets(compiler: KotlinJsCompilerType = defaultJsCompilerType) {
    js(compiler).configure()
}

fun KotlinTargetContainerWithJsPresetFunctions.jsTargets(compiler: KotlinJsCompilerType = defaultJsCompilerType) {
    js(compiler).configure()
}

fun KotlinTargetContainerWithPresetFunctions.jvmTargets() {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget        = "1.8"
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
            }
        }
    }
}

fun Project.installFullScreenDemo(suffix: String) {
    try {
        val webPack = project.tasks.getByName("jsBrowser${suffix}Webpack", org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class)

        tasks.register<Copy>("installFullScreenDemo$suffix") {
            dependsOn(webPack)

            val kotlinExtension = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
            val kotlinSourceSets = kotlinExtension.sourceSets

            val jsFile          = webPack.outputFile
            val commonResources = kotlinSourceSets.getByName("commonMain").resources
            val jsResources     = kotlinSourceSets.getByName("jsMain"    ).resources
            val docDirectory    = "$buildDir/../../docs/${project.name.toLowerCase().removeSuffix("runner")}"

            from(commonResources, jsResources, jsFile)
            into(docDirectory)
        }
    } catch (ignored: Exception) {}
}