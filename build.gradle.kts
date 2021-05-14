buildscript {
    val kotlinVersion: String by System.getProperties()

    repositories {
        maven       { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven       { url = uri("https://plugins.gradle.org/m2/"          ) }
        mavenCentral()
    }

    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}

plugins {
    kotlin("multiplatform") apply false
}

allprojects {
    repositories {
        maven       { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
        mavenLocal  ()
        mavenCentral()
        jcenter     ()
    }
}
