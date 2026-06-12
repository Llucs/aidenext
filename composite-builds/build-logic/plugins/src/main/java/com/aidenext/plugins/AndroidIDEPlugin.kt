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

package com.aidenext.plugins

import com.android.build.api.dsl.CommonExtension
import com.aidenext.build.config.isFDroidBuild
import com.aidenext.plugins.util.isAndroidModule
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Manages plugins applied on the IDE's project modules.
 *
 * @author Akash Yadav
 */
class AndroidIDEPlugin : Plugin<Project> {

  override fun apply(target: Project) = target.run {
    if (project.path == rootProject.path) {
      throw GradleException("Cannot apply ${AndroidIDEPlugin::class.simpleName} to root project")
    }

    if (!project.buildFile.exists() || !project.buildFile.isFile) {
      return@run
    }

    if (isAndroidModule && !isFDroidBuild) {
      // setup signing configuration
      plugins.apply(SigningConfigPlugin::class.java)
    }

    if (isFDroidBuild && project.plugins.hasPlugin("com.aidenext.core-app")) {
      val androidExt = extensions.getByType(CommonExtension::class.java)
      logger.warn("Building for F-Droid with configuration:")
      logger.warn("applicationId = ${androidExt.defaultConfig.applicationId}")
      logger.warn("versionName = ${androidExt.defaultConfig.versionName}")
      logger.warn("versionCode = ${androidExt.defaultConfig.versionCode}")
      logger.warn("--- x --- x ---")
    }

    val taskName = when {
      isAndroidModule -> "testDebugUnitTest"
      else -> "test"
    }

    logger.info("${project.path} will run task '$taskName' for tests in CI")

    project.tasks.register("runTestsInCI") {
      dependsOn(taskName)
    }
  }
}
