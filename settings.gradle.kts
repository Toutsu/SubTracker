pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "SubTracker"

include(
    ":shared",
    ":backend",
    ":web-frontend",
    ":telegram-bot"
)