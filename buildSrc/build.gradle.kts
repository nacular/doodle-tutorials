plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
    implementation(gradleApi())
}