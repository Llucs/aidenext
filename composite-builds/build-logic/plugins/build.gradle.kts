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

plugins {
  `kotlin-dsl`
}

repositories {
  google()
  gradlePluginPortal()
  mavenCentral()
}

kotlin {
  compilerOptions {
    jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
  }
}

dependencies {
  implementation(projects.buildLogic.common)
  implementation(projects.buildLogic.desugaring)
  implementation(projects.buildLogic.propertiesParser)

  compileOnly(libs.android.gradle.plugin)
  compileOnly(libs.kotlin.gradle.plugin)
  implementation(libs.maven.publish)

  implementation(libs.common.jkotlin)
  implementation(libs.common.antlr4)
  implementation(libs.google.gson)
  implementation(libs.google.java.format)
}

gradlePlugin {
  plugins {
    create("com.aidenext.build") {
      id = "com.aidenext.build"
      implementationClass = "com.aidenext.plugins.AndroidIDEPlugin"
    }
    create("com.aidenext.core-app") {
      id = "com.aidenext.core-app"
      implementationClass = "com.aidenext.plugins.AndroidIDECoreAppPlugin"
    }
    create("com.aidenext.build.propsparser") {
      id = "com.aidenext.build.propsparser"
      implementationClass = "com.aidenext.plugins.PropertiesParserPlugin"
    }
    create("com.aidenext.build.lexergenerator") {
      id = "com.aidenext.build.lexergenerator"
      implementationClass = "com.aidenext.plugins.LexerGeneratorPlugin"
    }
  }
}
