import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import java.util.*

/**
 * Helper to make using js platform in build.gradle.kts files simpler
 */
fun KotlinMultiplatformExtension.jsTargets(executable: Boolean = false) {
    js {
        browser {
            if (executable) {
                binaries.executable()
            }
        }
    }
}

/**
 * Helper to make using wasmJs platform in build.gradle.kts files simpler
 */
@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.wasmJsTargets(executable: Boolean = false) {
    wasmJs {
        browser {
            if (executable) {
                binaries.executable()
//                applyBinaryen {
//                    binaryenArgs += "-g" // keep original names
//                }
            }
        }
    }
}

/**
 * Helper to make using jvm platform in build.gradle.kts files simpler
 */
fun KotlinTargetContainerWithPresetFunctions.jvmPlatform(jvmTarget: String, vararg additoinalFlags: String) {
    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                this.jvmTarget   = jvmTarget
                freeCompilerArgs = additoinalFlags.toList()
            }
        }
    }
}

/**
 * Helper for determining OS - architecture pair for the build machine. This helper could be used by all Doodle desktop sourc-sets.
 */
fun osTarget(): String {
    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X"       -> "macos"
        osName.startsWith("Win"  ) -> "windows"
        osName.startsWith("Linux") -> "linux"
        else                       -> error("Unsupported OS: $osName")
    }

    val targetArch = when (val osArch = System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        "aarch64"         -> "arm64"
        else              -> error("Unsupported arch: $osArch")
    }

    return "${targetOs}-${targetArch}"
}

/**
 * Helper used to build full-screen versions of all apps an install them in the docs directory so they are accessible from the docs site
 */
fun Project.installFullScreenDemo(suffix: String) {
    try {
        val jsWebPack   = tasks.getByName("jsBrowser${suffix}Webpack",     org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class)
        val wasmWebPack = tasks.getByName("wasmJsBrowser${suffix}Webpack", org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class)

        val jsDocDirectory = "$buildDir/../../docs/${project.name.lowercase(Locale.getDefault()).removeSuffix("runner")}"

        val jsInstall = tasks.register<Copy>("jsInstallFullScreenDemo$suffix") {
            dependsOn(jsWebPack)

            from(jsWebPack.outputDirectory.asFileTree.files.filter { !it.name.endsWith(".map") })
            into(jsDocDirectory)
        }

        val wasmInstall = tasks.register<Copy>("wasmInstallFullScreenDemo$suffix") {
            dependsOn(wasmWebPack)

            from(wasmWebPack.outputDirectory.asFileTree.files.filter { !it.name.endsWith(".map") })
            into("${jsDocDirectory}_wasm")
        }

        tasks.register("installFullScreenDemo$suffix") {
            dependsOn(jsInstall  )
            dependsOn(wasmInstall)
        }
    } catch (ignored: Exception) {}
}