plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.20-RC"
    application
}

application {
    mainClass.set("MainKt")
}

sourceSets {
    main {
        kotlin {
            srcDirs("src")
        }
    }
}

repositories {
    mavenCentral()
}

val telegramBotVersion = "7.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    
    // Telegram Bot API
    implementation("com.github.pengrad:java-telegram-bot-api:$telegramBotVersion")
    
    // Для работы с корутинами
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    
    // Для сериализации JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
