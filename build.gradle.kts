plugins {
    // Ключевой плагин для KMP
    kotlin("multiplatform") version "2.0.0"
}

repositories {
    mavenCentral()
}

group = "com.subtracker"
version = "1.0.0"

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
    
    // Определяем общие настройки для всех платформ
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