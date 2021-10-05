buildscript {
    val kotlinVersion: String by System.getProperties()

    repositories {
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
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        mavenLocal  ()
        mavenCentral()
    }
}
