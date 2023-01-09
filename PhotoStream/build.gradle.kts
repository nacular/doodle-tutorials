plugins {
    kotlin("js"                  )
    kotlin("plugin.serialization")
}

kotlin {
    jsTargets(BOTH)

    val ktorVersion         : String by project
    val doodleVersion       : String by project
    val coroutinesVersion   : String by project
    val serializationVersion: String by project

    dependencies {
        implementation("io.ktor:ktor-client-core:$ktorVersion"                                 )
        implementation("io.ktor:ktor-client-serialization:$ktorVersion"                        )
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion"   )
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

        api("io.nacular.doodle:core:$doodleVersion"     )
        api("io.nacular.doodle:browser:$doodleVersion"  )
        api("io.nacular.doodle:controls:$doodleVersion" )
        api("io.nacular.doodle:themes:$doodleVersion"   )
    }
}