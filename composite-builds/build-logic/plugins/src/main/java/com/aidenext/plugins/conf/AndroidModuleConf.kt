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
    configureAppModule(coreLibDesugDep)
  } else {
    configureLibraryModule(coreLibDesugDep)
  }
}

private fun Project.configureAppModule(
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>
) {
  extensions.getByType(ApplicationExtension::class.java).apply {
    compileSdk = BuildConfig.compileSdk

    packaging.resources.excludes += listOf(
      "META-INF/CHANGES",
      "META-INF/README.md",
    )
    packaging.resources.pickFirsts += listOf(
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

    defaultConfig {
      minSdk = BuildConfig.minSdk
      targetSdk = BuildConfig.targetSdk
      versionCode = projectVersionCode
      versionName = rootProject.version.toString().removePrefix("v")
      multiDexEnabled = true
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions.sourceCompatibility = BuildConfig.javaVersion
    compileOptions.targetCompatibility = BuildConfig.javaVersion

    this@configureAppModule.configureDesugaring(this as CommonExtension<*, *, *, *, *>, coreLibDesugDep)

    if (project.plugins.hasPlugin("com.aidenext.core-app")) {
      packaging.jniLibs.useLegacyPackaging = true
    } else {
      defaultConfig {
        ndk {
          abiFilters.clear()
          abiFilters += flavorsAbis.keys
        }
      }
    }

    buildTypes.named("debug") { isMinifyEnabled = false }
    buildTypes.named("release") {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    buildTypes.register("dev") {
      initWith(buildTypes.named("release").get())
      isMinifyEnabled = false
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    buildFeatures.viewBinding = true
    buildFeatures.buildConfig = true
  }
}

private fun Project.configureLibraryModule(
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>
) {
  extensions.getByType(LibraryExtension::class.java).apply {
    compileSdk = BuildConfig.compileSdk

    packaging.resources.excludes += listOf(
      "META-INF/CHANGES",
      "META-INF/README.md",
    )
    packaging.resources.pickFirsts += listOf(
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

    compileOptions.sourceCompatibility = BuildConfig.javaVersion
    compileOptions.targetCompatibility = BuildConfig.javaVersion

    defaultConfig {
      multiDexEnabled = true
    }

    this@configureLibraryModule.configureDesugaring(this as CommonExtension<*, *, *, *, *>, coreLibDesugDep)

    buildTypes.named("debug") { isMinifyEnabled = false }
    buildTypes.named("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    buildTypes.register("dev") {
      initWith(buildTypes.named("release").get())
      isMinifyEnabled = false
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    buildFeatures.viewBinding = true
    buildFeatures.buildConfig = true
  }
}

private fun Project.configureDesugaring(
  extension: CommonExtension<*, *, *, *, *>,
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>
) {
  val enabled = !plugins.hasPlugin(NoDesugarPlugin::class.java)
  extension.compileOptions.apply {
    isCoreLibraryDesugaringEnabled = enabled
  }
  if (enabled) {
    dependencies.add("coreLibraryDesugaring", coreLibDesugDep)
  }
}
