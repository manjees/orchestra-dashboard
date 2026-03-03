pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "orchestra-dashboard"

include(":shared")
include(":androidApp")
include(":desktopApp")
include(":server")
