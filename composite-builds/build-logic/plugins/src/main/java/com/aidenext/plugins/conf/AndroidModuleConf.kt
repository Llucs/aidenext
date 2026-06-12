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

/**
 * ABIs for which the product flavors will be created.
 * The keys in this map are the names of the product flavors whereas,
 * the value for each flavor is a number that will be incremented to the base version code of the IDE
 * and set as the version code of that flavor.
 *
 * For example, if the base version code of the IDE is 270 (for v2.7.0), then for arm64-v8a
 * flavor, the version code will be `100 * 270 + 1` i.e. `27001`
 */
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

  val androidExt = if (isAppModule) {
    extensions.getByType(ApplicationExtension::class.java)
  } else {
    extensions.getByType(LibraryExtension::class.java)
  }

  androidExt.apply {
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
  }

  androidExt.run {
    compileSdkVersion(BuildConfig.compileSdk)

    defaultConfig {
      minSdk = BuildConfig.minSdk
      targetSdk = BuildConfig.targetSdk
      versionCode = projectVersionCode
      versionName = rootProject.version.toString().removePrefix("v")

      // required
      multiDexEnabled = true

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
      sourceCompatibility = BuildConfig.javaVersion
      targetCompatibility = BuildConfig.javaVersion
    }

    configureCoreLibDesugaring(this, coreLibDesugDep)

    if (project.plugins.hasPlugin("com.aidenext.core-app")) {
      packaging {
        jniLibs {
          useLegacyPackaging = true
        }
      }

      splits {
        abi {
          reset()
          isEnable = true
          isUniversalApk = false
          if (isFDroidBuild) {
            include(FDroidConfig.fDroidBuildArch!!)
          } else {
            include(*flavorsAbis.keys.toTypedArray())
          }
        }
      }

      extensions.getByType(ApplicationAndroidComponentsExtension::class.java).apply {
        onVariants { variant ->
          variant.outputs.forEach { output ->

            // version code increment
            val verCodeIncr = flavorsAbis[output.getFilter(
              FilterConfiguration.FilterType.ABI
            )?.identifier]
              ?: throw UnsupportedOperationException("Universal APKs are not supported!")

            output.versionCode.set(100 * projectVersionCode + verCodeIncr)
          }
        }
      }
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

      // from AGP 8.4.0 onwards, there are some behavioral changes in R8
      // enabling R8 on library projects results in missing class errors
      // see https://issuetracker.google.com/issues/338411137#comment11
      isMinifyEnabled = isAppModule
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }

    // development build type
    // similar to 'release', but disables proguard/r8
    // this build type can be used to gain release-like performance at runtime
    // the build are faster for this build type as compared to 'release'
    buildTypes.register("dev") {
      initWith(buildTypes.named("release").get())
      isMinifyEnabled = false
    }

    testOptions { unitTests.isIncludeAndroidResources = true }

    buildFeatures.viewBinding = true
    buildFeatures.buildConfig = true
  }
}

private fun Project.configureCoreLibDesugaring(
  baseExtension: CommonExtension<*, *, *, *, *>,
  coreLibDesugDep: Provider<MinimalExternalModuleDependency>
) {
  val coreLibDesugaringEnabled = !project.plugins.hasPlugin(NoDesugarPlugin::class.java)

  baseExtension.compileOptions.isCoreLibraryDesugaringEnabled = coreLibDesugaringEnabled

  if (coreLibDesugaringEnabled) {
    project.dependencies.add("coreLibraryDesugaring", coreLibDesugDep)
  }
}