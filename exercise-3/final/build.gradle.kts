import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.kotlin.kapt") version "1.6.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.7.0"
    id("gg.jte.gradle") version "1.12.1"
    id("com.google.protobuf") version "0.9.2"
}

version = "0.1.0"
group = "io.confluent.examples.autonomo"

val kotlinVersion = project.properties["kotlinVersion"]

dependencies {
    // Language
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")

    // Serialization
    implementation("com.google.protobuf:protobuf-kotlin:3.21.12")
    implementation("com.google.protobuf:protobuf-java-util:3.21.12")

    // Streaming Data Platform
    implementation("org.apache.kafka:kafka-streams:3.3.1")
    implementation("io.confluent:kafka-streams-protobuf-serde:7.3.1")

    // Micronaut App Framework
    kapt("io.micronaut:micronaut-http-validation")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.views:micronaut-views-jte")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("io.micronaut:micronaut-validation")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Logging
    runtimeOnly("ch.qos.logback:logback-classic")

    // Testing
    testImplementation("org.apache.kafka:kafka-streams-test-utils:3.3.1")
}


application {
    mainClass.set("io.confluent.examples.autonomo.ApplicationKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion("11")
    targetCompatibility = JavaVersion.toVersion("11")
}

graalvmNative.toolchainDetection.set(false)

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("io.confluent.examples.autonomo.*")
    }
}

jte {
    sourceDirectory.set(file("src/main/jte").toPath())
    generate()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.2"
    }

    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("kotlin")
            }
        }
    }
}
