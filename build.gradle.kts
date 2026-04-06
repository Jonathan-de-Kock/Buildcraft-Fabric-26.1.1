plugins {
    id("net.fabricmc.fabric-loom") version "1.16.1" apply false
}

val modVersion: String by project
val mavenGroup: String by project

allprojects {
    group = mavenGroup
    version = modVersion
}
