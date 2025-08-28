plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.0"
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    port = 3000
                    open = true
                }
            }
        }
        nodejs()
    }
    
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
            }
        }
        
        jsMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:1.0.0-pre.710")
            }
        }
        
        jsTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-js"))
            }
        }
    }
}

// Простая задача для запуска веб-сервера
tasks.register("serve") {
    dependsOn("build")
    doLast {
        println("Web frontend built successfully!")
        println("Open web-frontend/build/processedResources/js/main/index.html in your browser")
        println("Or use a simple HTTP server:")
        println("cd web-frontend/build/processedResources/js/main && python -m http.server 3000")
    }
}

// Задача для запуска веб-фронтенда
tasks.register("run") {
    dependsOn("build")
    doLast {
        println("Starting web frontend server...")
        exec {
            workingDir = file("build/processedResources/js/main")
            commandLine("python", "-m", "http.server", "3000")
        }
    }
}