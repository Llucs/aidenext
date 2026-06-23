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

package com.aidenext.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.logging.Logging

/**
 * Plugin to manage LogSender in Android applications.
 *
 * @author Akash Yadav
 */
class LogSenderPlugin : Plugin<Project> {

  companion object {

    private const val LOGSENDER_DEPENDENCY_ARTIFACT = "logsender"

    private val logger = Logging.getLogger(LogSenderPlugin::class.java)
  }

  override fun apply(target: Project) {
    if (target.isTestEnv) {
      logger.lifecycle("Applying ${javaClass.simpleName} to project '${target.path}'")
    }

    target.run {

      check(plugins.hasPlugin(APP_PLUGIN)) {
        "${javaClass.simpleName} can only be applied to Android application projects."
      }

      extensions.getByType(AndroidComponentsExtension::class.java).apply {

        val debuggableBuilds = hashSetOf<String>()

        beforeVariants { variantBuilder ->
          val isDebug = variantBuilder.buildType == "debug"
          logger.info("Variant :'${variantBuilder.name}' isDebuggable: $isDebug")
          if (isDebug) {
            debuggableBuilds.add(variantBuilder.name)
          }
        }

        onVariants { variant ->
          logger.info(
            "Found ${debuggableBuilds.size} debuggable builds in project '${project.path}'" +
                ": $debuggableBuilds"
          )

          if (debuggableBuilds.isEmpty()) {
            logger.warn("No debuggable builds found in project '${project.path}'")
          }

          if (variant.name in debuggableBuilds) {
            val logsenderDependency = project.dependencies.ideDependency(
              LIB_GROUP_LOGGING,
              LOGSENDER_DEPENDENCY_ARTIFACT,
              project.isTestEnv
            )

            if (logsenderDependency is ExternalModuleDependency) {
              logger.debug("Marking logsender dependency as not-changing")
              logsenderDependency.isChanging = false
            }

            logger.lifecycle(
              "Adding LogSender dependency (version '${logsenderDependency.version}')" +
                  " to variant '${variant.name}' of project '${project.path}'"
            )

            logger.debug("Adding logsender dependency: $logsenderDependency")
            variant.runtimeConfiguration?.let { config ->
              config.dependencies.add(logsenderDependency)
            }
          }
        }
      }
    }
  }
}
