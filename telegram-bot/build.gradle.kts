plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.0"
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

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    
    // Telegram Bot API (ktgbotapi)
    implementation("dev.inmo:tgbotapi:9.4.0")
    
    // Для работы с корутинами
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    
    // Для сериализации JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
    
    // Для HTTP запросов к бэкенду
    implementation("io.ktor:ktor-client-core:2.3.11")
    implementation("io.ktor:ktor-client-cio:2.3.11")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
}
