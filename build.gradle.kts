import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

subprojects {
    repositories {
        mavenLocal  ()
        mavenCentral()
        maven       { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven       { url = uri("https://oss.sonatype.org/content/repositories/staging/") } // staging
    }

    tasks.withType<KotlinNpmInstallTask> {
        args += "--ignore-scripts"
    }

    afterEvaluate {
        installFullScreenDemo("Development")
        installFullScreenDemo("Production")
    }
}