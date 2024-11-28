@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

//sampleStart
plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    js     { browser { binaries.executable() } } // Web     (JS  ) executable
    wasmJs { browser { binaries.executable()     // Web     (WASM) executable
        applyBinaryen {}                         // Binary size optimization
    } }
    jvm    {                                     // Desktop (JVM ) executable
        compilations.all {
            kotlinOptions { jvmTarget = "11" }   // JVM 11 is needed for Desktop
        }
        withJava()
    }

    sourceSets {
        // Source set for all platforms
        commonMain.dependencies {
            api(libs.coroutines.core) // async photo loading

            api(libs.doodle.themes   )
            api(libs.doodle.controls )
            api(libs.doodle.animation)
        }

        // Web (JS) platform source set
        jsMain.dependencies {
            implementation(libs.doodle.browser)
        }

        // Web (WASM) platform source set
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.doodle.browser)
            }
        }

        // Desktop (JVM) platform source set
        jvmMain.dependencies {
            // helper to derive OS/architecture pair
            when (osTarget()) {
                "macos-x64"     -> implementation(libs.doodle.desktop.jvm.macos.x64    )
                "macos-arm64"   -> implementation(libs.doodle.desktop.jvm.macos.arm64  )
                "linux-x64"     -> implementation(libs.doodle.desktop.jvm.linux.x64    )
                "linux-arm64"   -> implementation(libs.doodle.desktop.jvm.linux.arm64  )
                "windows-x64"   -> implementation(libs.doodle.desktop.jvm.windows.x64  )
                "windows-arm64" -> implementation(libs.doodle.desktop.jvm.windows.arm64)
            }
        }
    }
}

// Desktop entry point
application {
    mainClass.set("io.nacular.doodle.examples.MainKt")
}
//sampleEnd

// could be moved to buildSrc, but kept here for clarity
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