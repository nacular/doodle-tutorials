plugins {
    kotlin("js")
}

kotlin {
    jsTargets()

    dependencies {
        implementation(project(":Calculator"))
    }
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )