package buildsrc.conventions

import org.jetbrains.kotlin.gradle.plugin.mpp.*

plugins {
    id("buildsrc.conventions.kotlin-base")
}

kotlin {
    // Native targets all extend commonMain and commonTest.
    //
    // common/
    // └── native/
    //     ├── linux/
    //     │   └── linuxX64
    //     │── windows/
    //     │   └── mingwX64
    //     │── apple/
    //     │   ├── macosX64
    //     │   └── macosArm64
    //     └── ios/
    //         ├── iosArm64
    //         ├── iosSimulatorArm64
    //         └── iosX64

    mingwX64()
    linuxX64()

    macosX64()
    macosArm64()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {}
        val commonTest by getting {}

        val nativeMain by creating { dependsOn(commonMain) }
        val nativeTest by creating { dependsOn(commonTest) }

        // Linux
        val linuxMain by creating { dependsOn(nativeMain) }
        val linuxTest by creating { dependsOn(nativeTest) }

        val linuxX64Main by getting { dependsOn(linuxMain) }
        val linuxX64Test by getting { dependsOn(linuxTest) }

        // Windows
        val windowsMain by creating { dependsOn(nativeMain) }
        val windowsTest by creating { dependsOn(nativeTest) }

        val mingwX64Main by getting { dependsOn(windowsMain) }
        val mingwX64Test by getting { dependsOn(windowsTest) }

        // macOS
        val macosMain by creating { dependsOn(nativeMain) }
        val macosTest by creating { dependsOn(nativeTest) }

        val macosX64Main by getting { dependsOn(macosMain) }
        val macosX64Test by getting { dependsOn(macosTest) }

        val macosArm64Main by getting { dependsOn(macosMain) }
        val macosArm64Test by getting { dependsOn(macosTest) }

        // iOS
        val iosMain by creating { dependsOn(nativeMain) }
        val iosTest by creating { dependsOn(nativeTest) }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosX64Test by getting { dependsOn(iosTest) }

        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosArm64Test by getting { dependsOn(iosTest) }

        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Test by getting { dependsOn(iosTest) }
    }
}
