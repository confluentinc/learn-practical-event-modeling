plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
}

group = "io.confluent.examples.autonomous-vehicle-rideshare"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("org.valiktor:valiktor-core:0.12.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}