plugins {
    buildsrc.conventions.`kotlin-js`
    buildsrc.conventions.`kotlin-native`
    buildsrc.conventions.`kotlin-jvm`
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    buildsrc.conventions.publishing
}

group = "io.github.xn32"
version = "0.4.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
