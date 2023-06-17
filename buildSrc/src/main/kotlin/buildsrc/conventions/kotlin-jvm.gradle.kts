package buildsrc.conventions

import org.gradle.kotlin.dsl.jvm

/** conventions for a Kotlin/JVM subproject */

plugins {
    id("buildsrc.conventions.kotlin-base")
}

kotlin {
    jvm {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }

        testRuns.configureEach {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
}
