buildscript {
    val kotlinVersion: String by System.getProperties()

    repositories {
        maven       { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven       { url = uri("https://plugins.gradle.org/m2/"          ) }
        mavenCentral()
    }

    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    kotlin("multiplatform")
}

allprojects {
    repositories {
        maven       { url = uri("http://dl.bintray.com/kotlin/kotlin-eap") }
        mavenLocal  ()
        mavenCentral()
        jcenter     ()
    }
}