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

package com.aidenext.plugins.conf

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.impl.getFilter
import com.aidenext.build.config.BuildConfig
import com.aidenext.build.config.FDroidConfig
import com.aidenext.build.config.isFDroidBuild
import com.aidenext.build.config.projectVersionCode
import com.aidenext.plugins.NoDesugarPlugin
import com.aidenext.plugins.util.SdkUtils.getAndroidJar
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

internal val flavorsAbis = mapOf("armeabi-v7a" to 1, "arm64-v8a" to 2, "x86_64" to 3)

fun Project.configureAndroidModule(
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>
) {
  val isAppModule = plugins.hasPlugin("com.android.application")
  assert(
    isAppModule || plugins.hasPlugin("com.android.library")
  ) {
    "${javaClass.simpleName} can only be applied to Android projects"
  }

  val androidJar = extensions.getByType(AndroidComponentsExtension::class.java)
    .getAndroidJar(assertExists = true)
  val frameworkStubsJar = findProject(":utilities:framework-stubs")!!.file("libs/android.jar")
    .also { it.parentFile.mkdirs() }

  if (!(frameworkStubsJar.exists() && frameworkStubsJar.isFile)) {
    androidJar.copyTo(frameworkStubsJar)
  }

  if (isAppModule) {
    extensions.configure<ApplicationExtension> {
      configureCommon(this@configureAndroidModule, this, coreLibDesugDep, isAppModule)

      defaultConfig {
        minSdk = BuildConfig.minSdk
        targetSdk = BuildConfig.targetSdk
        versionCode = projectVersionCode
        versionName = rootProject.version.toString().removePrefix("v")
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      }

      if (project.plugins.hasPlugin("com.aidenext.core-app")) {
        packaging {
          jniLibs {
            useLegacyPackaging = true
          }
        }

        extensions.getByType(ApplicationAndroidComponentsExtension::class.java).apply {
          onVariants { variant ->
            variant.outputs.forEach { output ->
              val verCodeIncr = flavorsAbis[output.getFilter(
                FilterConfiguration.FilterType.ABI
              )?.identifier]
                ?: throw UnsupportedOperationException("Universal APKs are not supported!")
              output.versionCode.set(100 * projectVersionCode + verCodeIncr)
            }
          }
        }
      }
    }
  } else {
    extensions.configure<LibraryExtension> {
      compileSdk = BuildConfig.compileSdk

      packaging {
        resources {
          excludes += listOf(
            "META-INF/CHANGES",
            "META-INF/README.md",
          )
          pickFirsts += listOf(
            "META-INF/eclipse.inf",
            "META-INF/LICENSE.md",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "META-INF/INDEX.LIST",
            "about_files/LICENSE-2.0.txt",
            "plugin.xml",
            "plugin.properties",
            "about.mappings",
            "about.properties",
            "about.ini",
            "modeling32.png"
          )
        }
      }

      configureCommonLib(this@configureAndroidModule, this, coreLibDesugDep)
    }
  }
}

private fun Project.configureCommon(
  project: Project,
  extension: CommonExtension,
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>,
  isAppModule: Boolean
) {
  extension.compileSdk = BuildConfig.compileSdk

  extension.packaging {
    resources {
      excludes += listOf(
        "META-INF/CHANGES",
        "META-INF/README.md",
      )
      pickFirsts += listOf(
        "META-INF/eclipse.inf",
        "META-INF/LICENSE.md",
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/INDEX.LIST",
        "about_files/LICENSE-2.0.txt",
        "plugin.xml",
        "plugin.properties",
        "about.mappings",
        "about.properties",
        "about.ini",
        "modeling32.png"
      )
    }
  }

  extension.compileOptions {
    sourceCompatibility = BuildConfig.javaVersion
    targetCompatibility = BuildConfig.javaVersion
  }

  configureCoreLibDesugaring(project, extension, coreLibDesugDep)

  extension.buildTypes.named("debug") { isMinifyEnabled = false }
  extension.buildTypes.named("release") {
    isMinifyEnabled = isAppModule
    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
  }
  extension.buildTypes.register("dev") {
    initWith(buildTypes.named("release").get())
    isMinifyEnabled = false
  }

  extension.testOptions { unitTests.isIncludeAndroidResources = true }

  extension.buildFeatures.viewBinding = true
  extension.buildFeatures.buildConfig = true
}

private fun Project.configureCommonLib(
  project: Project,
  extension: LibraryExtension,
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>
) {
  extension.compileOptions {
    sourceCompatibility = BuildConfig.javaVersion
    targetCompatibility = BuildConfig.javaVersion
  }

  configureCoreLibDesugaring(project, extension, coreLibDesugDep)

  extension.buildTypes.named("debug") { isMinifyEnabled = false }
  extension.buildTypes.named("release") {
    isMinifyEnabled = false
    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
  }
  extension.buildTypes.register("dev") {
    initWith(buildTypes.named("release").get())
    isMinifyEnabled = false
  }

  extension.testOptions { unitTests.isIncludeAndroidResources = true }

  extension.buildFeatures.viewBinding = true
  extension.buildFeatures.buildConfig = true
}

private fun Project.configureCoreLibDesugaring(
  project: Project,
  baseExtension: CommonExtension,
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>
) {
  val coreLibDesugaringEnabled = !project.plugins.hasPlugin(NoDesugarPlugin::class.java)

  baseExtension.compileOptions.isCoreLibraryDesugaringEnabled = coreLibDesugaringEnabled

  if (coreLibDesugaringEnabled) {
    project.dependencies.add("coreLibraryDesugaring", coreLibDesugDep)
  }
}