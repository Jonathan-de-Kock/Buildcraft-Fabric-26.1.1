plugins {
    id("net.fabricmc.fabric-loom")
    java
}

val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricApiVersion: String by rootProject

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    api(project(":buildcraft-core"))
    api(project(":buildcraft-transport"))
    api(project(":buildcraft-silicon"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.withType<ProcessResources> {
    val modVersion: String by rootProject
    inputs.property("version", modVersion)
    filesMatching("fabric.mod.json") {
        expand("version" to modVersion)
    }
}
