plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    jsTargets    (executable = true)
    wasmJsTargets(executable = true)
    jvmTargets   (                 )

    sourceSets {
        commonMain.dependencies {
            implementation(project(":Contacts"))
        }

        jsMain.dependencies {
            implementation(libs.doodle.browser)
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
    }
}

application {
    mainClass.set("MainKt")
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )