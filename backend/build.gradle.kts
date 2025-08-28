plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.20-RC"
    id("io.ktor.plugin") version "2.3.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

sourceSets {
    main {
        kotlin {
            srcDirs("src/jvmMain")
        }
    }
}

application {
    mainClass.set("ApplicationKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.0"
val exposedVersion = "1.0.0-beta-2"
val postgresqlVersion = "42.7.3"

kotlin {
    jvmToolchain(17)
}

tasks {
    shadowJar {
        archiveBaseName.set("backend")
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = "ApplicationKt"
        }
    }
}

dependencies {
    implementation(project(":shared"))
    
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    // Добавлены зависимости для аутентификации
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("com.h2database:h2:2.2.224")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
