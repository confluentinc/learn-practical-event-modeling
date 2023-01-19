plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.kotlin.kapt") version "1.6.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.5.3"
}

group = "io.confluent.examples.autonomous-vehicle-rideshare"
version = "0.1.0"

val kotlinVersion = project.properties["kotlinVersion"]

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Language & Platform
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Domain
    implementation("com.fraktalio.fmodel:domain:3.2.0")
    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("org.valiktor:valiktor-core:0.12.0")

    // Application
    implementation("com.fraktalio.fmodel:application-arrow:3.2.0")

    // Adapters
    implementation("org.apache.kafka:kafka-streams:3.2.0")

    kapt("io.micronaut:micronaut-http-validation")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("io.micronaut:micronaut-validation")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Logging
    runtimeOnly("ch.qos.logback:logback-classic")
}


application {
    mainClass.set("io.confluent.examples.autonomo.mainKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("io.confluent.examples.*")
    }
}
