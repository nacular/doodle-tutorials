plugins {
    kotlin("js")
}

kotlin {
    jsTargets()

    dependencies {
        implementation(project(":Photos"))
    }
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )