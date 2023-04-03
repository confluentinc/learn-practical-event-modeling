rootProject.name="autonomo-step-1"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "confluent"
            url = uri("https://packages.confluent.io/maven")
        }
    }
}
