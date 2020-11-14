pluginManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "kotlin-multiplatform" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}

enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "doodle-tutorials"

include(
    "Calculator",
    "DocApps"
)