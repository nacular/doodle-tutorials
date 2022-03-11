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
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        mavenCentral()
    }
}
