plugins {
    // Ключевой плагин для KMP
    kotlin("multiplatform") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/detekt.yml")
    
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
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