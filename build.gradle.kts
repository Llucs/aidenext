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

@file:Suppress("UnstableApiUsage")

import com.aidenext.build.config.BuildConfig
import com.aidenext.build.config.FDroidConfig
import com.aidenext.build.config.publishingVersion
import com.aidenext.plugins.AndroidIDEPlugin
import com.aidenext.plugins.conf.configureAndroidModule
import com.aidenext.plugins.conf.configureJavaModule
import com.aidenext.plugins.conf.configureMavenPublish
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("build-logic.root-project")
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.gradle.publish) apply false
  alias(libs.plugins.protobuf) apply false
  alias(libs.plugins.benchmark) apply false
}

buildscript {
  dependencies {
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.nav.safe.args.gradle.plugin)
    classpath("org.ow2.asm:asm:9.7")
  }
}

// Root project has 'com.aidenext' as the group ID
project.group = BuildConfig.packageName

val kotlinVersion = "2.0.21"

subprojects {
  if (project != rootProject) {
    var group = project.parent!!.group
    if (project.parent != rootProject) {
      group = "${group}.${project.parent!!.name}"
    }
    project.group = group
  }

  configurations.all {
    resolutionStrategy {
      eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
          useVersion(kotlinVersion)
        }
      }
      force(
        "org.ow2.asm:asm:9.7",
        "org.ow2.asm:asm-commons:9.7",
        "org.ow2.asm:asm-tree:9.7",
        "org.ow2.asm:asm-analysis:9.7",
        "org.ow2.asm:asm-util:9.7",
      )
    }
  }

  // Always load the F-Droid config
  FDroidConfig.load(project)

  afterEvaluate {
    apply { plugin(AndroidIDEPlugin::class.java) }
  }

  project.version = rootProject.version

  plugins.withId("com.android.application") {
    pluginManager.apply("org.jetbrains.kotlin.android")
    configureAndroidModule(libs.androidx.libDesugaring)
  }
  plugins.withId("com.android.library") {
    pluginManager.apply("org.jetbrains.kotlin.android")
    configureAndroidModule(libs.androidx.libDesugaring)
  }

  plugins.withId("java-library") { configureJavaModule() }
  plugins.withId("com.vanniktech.maven.publish.base") { configureMavenPublish() }

  plugins.withId("com.gradle.plugin-publish") {
    configure<GradlePluginDevelopmentExtension> {
      version = project.publishingVersion
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      freeCompilerArgs.add("-Xstring-concat=inline")
      freeCompilerArgs.add("-Xskip-metadata-version-check")
      if (!project.path.startsWith(":tooling")) {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(BuildConfig.javaVersion.toString())
      }
    }
  }
}

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }
