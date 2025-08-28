plugins {
    kotlin("multiplatform") version "2.2.20-RC"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20-RC"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
    
    // Определяем версии библиотек
    val coroutinesVersion = "1.8.0"
    val serializationVersion = "1.6.3"
    
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
            }
        }
        
        jvmMain {
            dependencies {
                // Зависимости для JVM
            }
        }
        
        jsMain {
            dependencies {
                // Зависимости для JS
            }
        }
    }
}