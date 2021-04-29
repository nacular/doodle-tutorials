plugins {
    kotlin("js")
}

kotlin {
    jsTargets()

    dependencies {
        implementation(project(":Todo"))
    }
}

installFullScreenDemo("Development")
installFullScreenDemo("Production" )