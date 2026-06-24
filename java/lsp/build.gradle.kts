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


import com.aidenext.build.config.BuildConfig

plugins {
  id("com.android.library")
  id("com.google.devtools.ksp") version libs.versions.ksp
}

android {
  namespace = "${BuildConfig.packageName}.lsp.java"

  defaultConfig {
    javaCompileOptions {
      annotationProcessorOptions {
        arguments["eventBusIndex"] = "${BuildConfig.packageName}.events.LspJavaEventsIndex"
      }
    }
  }

  sourceSets {
    getByName("androidTest") {
      assets.directories.add(rootProject.file("utilities/framework-stubs/libs"))
    }
  }
}

dependencies {
  ksp(projects.annotation.processorsKsp)
  annotationProcessor(libs.google.auto.service)

  api(projects.core.indexingApi)

  implementation(libs.androidide.ts)
  implementation(libs.androidide.ts.java)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.appcompat)
  implementation(libs.common.editor)
  implementation(libs.common.javaparser)
  implementation(libs.common.utilcode)

  implementation(libs.google.auto.service.annotations)
  implementation(libs.google.guava)
  implementation(libs.google.gson)
  implementation(libs.google.material)

  implementation(projects.core.actions)
  implementation(projects.core.common)
  implementation(projects.core.lspApi)
  implementation(projects.core.resources)
  implementation(projects.editor.api)
  implementation(projects.java.javacServices)

  implementation(libs.composite.javac)
  implementation(libs.composite.javapoet)
  implementation(libs.composite.jaxp)
  implementation(libs.composite.jdkJdeps)
  implementation(libs.composite.jdt)
  implementation(libs.composite.googleJavaFormat)

  implementation(libs.androidx.core.ktx)
  implementation(libs.common.kotlin)
  
  testImplementation(projects.testing.commonTest)
  testImplementation(projects.testing.lspTest)

  androidTestImplementation(projects.testing.androidTest)
  androidTestImplementation(projects.utilities.shared)
}
