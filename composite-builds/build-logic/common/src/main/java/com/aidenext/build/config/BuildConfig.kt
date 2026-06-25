package com.aidenext.build.config

import org.gradle.api.JavaVersion

object BuildConfig {
  const val packageName = "com.aidenext"

  const val compileSdk = 36

  const val minSdk = 26

  const val targetSdk = 36

  const val ndkVersion = "27.0.12077973"

  val javaVersion = JavaVersion.VERSION_17
}
