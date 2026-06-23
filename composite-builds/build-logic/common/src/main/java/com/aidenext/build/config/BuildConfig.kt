package com.aidenext.build.config

import org.gradle.api.JavaVersion

object BuildConfig {
  const val packageName = "com.aidenext"

  const val compileSdk = 34

  const val minSdk = 26

  const val targetSdk = 34

  const val ndkVersion = "27.1.12296606"

  val javaVersion = JavaVersion.VERSION_17
}
