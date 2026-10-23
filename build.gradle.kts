import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

subprojects {
    repositories {
        mavenCentral()
        mavenLocal  ()
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }

    tasks.withType<KotlinNpmInstallTask> {
        args += "--ignore-scripts"
    }

    afterEvaluate {
        installFullScreenDemo("Development")
        installFullScreenDemo("Production")
    }
}