rootProject.name = "buildSrc"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    pluginManagement {
        repositories {
            mavenCentral()
            gradlePluginPortal()
        }
    }
}
