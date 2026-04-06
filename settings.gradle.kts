pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "BuildCraft"

include("buildcraft-api")
include("buildcraft-lib")
include("buildcraft-core")
include("buildcraft-transport")
include("buildcraft-energy")
include("buildcraft-factory")
include("buildcraft-silicon")
include("buildcraft-builders")
include("buildcraft-robotics")
include("buildcraft-all")
