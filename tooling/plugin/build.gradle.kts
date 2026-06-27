/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */


import com.aidenext.build.config.AGP_VERSION_MINIMUM
import com.aidenext.build.config.BuildConfig
import com.aidenext.build.config.ProjectConfig

plugins {
  id("java-gradle-plugin")
  id("org.jetbrains.kotlin.jvm")
  id("com.vanniktech.maven.publish.base")
}

kotlin {
  compilerOptions {
    jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}



description = "Gradle Plugin for projects that are built with AndroidIDE"

tasks.named<Test>("test") {
  useJUnitPlatform()
}

configurations {
  val androidBuildTool = create("androidBuildTool")

  named("compileOnly") {
    extendsFrom(androidBuildTool)
  }
  named("testImplementation") {
    extendsFrom(androidBuildTool)
  }
  findByName("integrationTestImplementation")?.run {
    extendsFrom(androidBuildTool)
  }
}

dependencies {
  implementation(projects.tooling.pluginConfig)
  implementation(projects.utilities.buildInfo)

  // use the AGP APIs from the minimum supported AGP version
  add("androidBuildTool", "com.android.tools.build:gradle:${AGP_VERSION_MINIMUM}")

  testImplementation(gradleTestKit())
  testImplementation(libs.tests.junit.jupiter)
  testImplementation(libs.tests.google.truth)
  testImplementation(projects.utilities.shared)

  testRuntimeOnly(libs.tests.junit.platformLauncher)
}

gradlePlugin {
  website.set(ProjectConfig.REPO_URL)
  vcsUrl.set(ProjectConfig.REPO_URL)

  plugins {
    create("initScriptPlugin") {
      id = "${BuildConfig.packageName}.init"
      implementationClass = "${BuildConfig.packageName}.gradle.AndroidIDEInitScriptPlugin"
      displayName = "AndroidIDE Init Script Gradle Plugin"
      description = "Init script Gradle plugin for projects that are built with AndroidIDE"
      tags.set(setOf("androidide", "init"))
    }

    create("gradlePlugin") {
      id = BuildConfig.packageName
      implementationClass = "${BuildConfig.packageName}.gradle.AndroidIDEGradlePlugin"
      displayName = "AndroidIDE Gradle Plugin"
      description = "Gradle plugin for projects that are built with AndroidIDE"
      tags.set(setOf("androidide", "gradle"))
    }

    create("logsenderPlugin") {
      id = "${BuildConfig.packageName}.logsender"
      implementationClass = "${BuildConfig.packageName}.gradle.LogSenderPlugin"
      displayName = "AndroidIDE LogSender Gradle Plugin"
      description = "Gradle plugin for applying LogSender-specific configuration to projects that are built with AndroidIDE"
      tags.set(setOf("androidide", "logsender"))
    }
  }
}
