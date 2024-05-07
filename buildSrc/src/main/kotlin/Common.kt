import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import java.util.*

private fun KotlinJsTargetDsl.configure(executable: Boolean) {
    compilations.configureEach {
        kotlinOptions {
            moduleKind            = "umd"
            sourceMapEmbedSources = "always"
        }
    }
    browser {
        testTask {
            enabled = false
        }

        if (executable) {
            binaries.executable()
        }
    }
}

fun KotlinMultiplatformExtension.jsTargets(executable: Boolean = false) {
    js().configure(executable)
}

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.wasmJsTargets(executable: Boolean = false) {
    wasmJs {
        compilations.all {
            kotlinOptions {
                moduleKind            = "umd"
                sourceMapEmbedSources = "always"
            }
        }
        browser {
            testTask { enabled = false }
        }
        if (executable) {
            binaries.executable()
//            applyBinaryen {
//                binaryenArgs += "-g" // keep original names
//            }

//            if (project.gradle.startParameter.taskNames.find { it.contains("wasmJsBrowserProductionWebpack") } != null) {
//                applyBinaryen {
//                    binaryenArgs = mutableListOf(
//                        "--enable-nontrapping-float-to-int",
//                        "--enable-gc",
//                        "--enable-reference-types",
//                        "--enable-exception-handling",
//                        "--enable-bulk-memory",
//                        "--inline-functions-with-loops",
//                        "--traps-never-happen",
//                        "--fast-math",
//                        "--closed-world",
//                        "--metrics",
//                        "-O3", "--gufa", "--metrics",
//                        "-O3", "--gufa", "--metrics",
//                        "-O3", "--gufa", "--metrics",
//                    )
//                }
//            }
        }
    }
}

fun KotlinTargetContainerWithPresetFunctions.jvmTargets(jvmTargetOverrid: String = "11", vararg additoinalFlags: String) {
    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget        = jvmTargetOverrid
                freeCompilerArgs = additoinalFlags.toList()
            }
        }
    }
}

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

fun Project.installFullScreenDemo(suffix: String) {
    try {
        val jsWebPack   = project.tasks.getByName("jsBrowser${suffix}Webpack",     org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class)
        val wasmWebPack = project.tasks.getByName("wasmJsBrowser${suffix}Webpack", org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class)

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