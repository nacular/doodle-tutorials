buildscript {
    val kotlinVersion: String by System.getProperties()

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

plugins {
    kotlin("multiplatform") apply false
}

allprojects {
    repositories {
        mavenLocal  ()
        mavenCentral()
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven       { url = uri("https://oss.sonatype.org/content/repositories/staging/") } // staging
    }

    tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask> {
        args += "--ignore-scripts"
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().download                    = false
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion                 = "16.0.0"
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.webpackCli.version = "4.10.0"
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().disableGranularWorkspaces()
}