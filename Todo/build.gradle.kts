plugins {
    kotlin("multiplatform"          )
    alias(libs.plugins.serialization)
    application
}

kotlin {
    // Defined in buildSrc/src/main/kotlin/Common.kt
    jsTargets    (executable = true)
    jvmTargets   ()
    wasmJsTargets(executable = true)

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core   )
            api(libs.serialization.json)

            api(libs.doodle.themes     )
            api(libs.doodle.controls   )
        }

        jsMain.dependencies {
            implementation(libs.doodle.browser)
        }

        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.doodle.browser)
            }
        }

        jvmMain.dependencies {
            when (osTarget()) {
                "macos-x64"     -> implementation(libs.doodle.desktop.jvm.macos.x64    )
                "macos-arm64"   -> implementation(libs.doodle.desktop.jvm.macos.arm64  )
                "linux-x64"     -> implementation(libs.doodle.desktop.jvm.linux.x64    )
                "linux-arm64"   -> implementation(libs.doodle.desktop.jvm.linux.arm64  )
                "windows-x64"   -> implementation(libs.doodle.desktop.jvm.windows.x64  )
                "windows-arm64" -> implementation(libs.doodle.desktop.jvm.windows.arm64)
            }
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.bundles.test.libs)
        }
    }
}

application {
    mainClass.set("io.nacular.doodle.examples.MainKt")
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )