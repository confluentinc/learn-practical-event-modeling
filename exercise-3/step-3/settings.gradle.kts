rootProject.name="autonomo-step-3"

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
