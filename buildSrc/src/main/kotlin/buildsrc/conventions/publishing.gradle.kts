package buildsrc.conventions


import org.gradle.kotlin.dsl.signing
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

/** Publishing conventions */

plugins {
    id("buildsrc.conventions.base")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

val isReleaseVersion = provider { !version.toString().endsWith("SNAPSHOT") }
val sonatypeRepoUrl = isReleaseVersion.map { isRelease ->
    if (isRelease) {
        uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
    } else {
        uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

val javadoc by tasks.registering(Jar::class) {
    // Sonatype requires that all artifacts have a Javadoc JAR, even if it's empty
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("json5k")
            description.set("JSON5 library for Kotlin")
            url.set("https://github.com/xn32/json5k")

            scm {
                url.set("https://github.com/xn32/json5k")
                connection.set("scm:git:git://github.com/xn32/json5k.git")
                developerConnection.set("scm:git:ssh://git@github.com/xn32/json5k.git")
            }

            developers {
                developer {
                    id.set("xn32")
                    url.set("https://github.com/xn32")
                }
            }

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
        }
    }

    publications.withType<MavenPublication>().configureEach {
        artifact(javadoc)
    }

    repositories {
        maven(sonatypeRepoUrl) {
            credentials {
                username = sonatypeUsername ?: ""
                password = sonatypePassword ?: ""
            }
        }
    }
}


// Gradle hasn't updated the signing plugin to be compatible with lazy-configuration, so it needs weird workarounds:
afterEvaluate {
    // Register signatures in afterEvaluate, otherwise the signing plugin creates the signing tasks
    // too early, before all the publications are added.
    signing {
        isRequired = isReleaseVersion.get() && gradle.taskGraph.hasTask("publish")

        val signingKey: String? by project
        val signingPassword: String? by project

        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

// Without this there is a Gradle error (notice mismatch between publish task and sign names):
// > Reason: Task ':publishIosArm64PublicationToMavenLocal' uses this output of task ':signIosX64Publication' without declaring an explicit or implicit dependency.
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(tasks.withType<Sign>())
}

tasks.withType<DokkaTask>().configureEach {
    val githubRepo = "https://github.com/xn32/json5k"
    val footerMsg = "<a href='$githubRepo'>json5k on GitHub</a>"

    dokkaSourceSets {
        configureEach {
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx.serialization/"))
            }

            includes.from("dokka/index.md")

            sourceLink {
                val gitVersion = if (isReleaseVersion.get()) {
                    "v$version"
                } else {
                    "main"
                }

                localDirectory.set(file("src"))
                remoteUrl.set(URL("$githubRepo/blob/$gitVersion/src"))
                remoteLineSuffix.set("#L")
            }
        }
    }

    outputDirectory.set(buildDir.resolve("dokka"))
    suppressInheritedMembers.set(true)

    // add the 'dokka' dir as an input for up-to-date checks
    inputs.dir(layout.projectDirectory.dir("dokka"))
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """
                {
                    "footerMessage": "$footerMsg",
                    "customStyleSheets": [ "${file("dokka/custom.css")}" ]
                }
            """.trimIndent()
        )
    )
}
