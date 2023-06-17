package buildsrc.conventions

/** conventions for a Kotlin/JS subproject */

plugins {
  id("buildsrc.conventions.kotlin-base")
}

kotlin {
  targets {
    js(IR) {
      browser()
      nodejs()
    }
  }
}
