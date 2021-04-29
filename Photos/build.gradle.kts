plugins {
    kotlin("js")
}

kotlin {
    jsTargets()

    val doodleVersion    : String by project
    val coroutinesVersion: String by project

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")

        api("io.nacular.doodle:core:$doodleVersion"     )
        api("io.nacular.doodle:browser:$doodleVersion"  )
        api("io.nacular.doodle:controls:$doodleVersion" )
        api("io.nacular.doodle:animation:$doodleVersion")
        api("io.nacular.doodle:themes:$doodleVersion"   )
    }
}