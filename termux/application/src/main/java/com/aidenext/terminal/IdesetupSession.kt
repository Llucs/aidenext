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

package com.aidenext.terminal

import android.content.Context
import com.aidenext.managers.ToolsManager
import com.aidenext.utils.Environment
import com.termux.shared.file.FileUtils
import com.termux.shared.shell.command.ExecutionCommand
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession
import com.termux.terminal.TerminalSession
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets

class IdesetupSession private constructor(
  private val termuxSession: TermuxSession,
  private val script: File
) {
  val terminalSession: TerminalSession
    get() = termuxSession.terminalSession

  val executionCommand: ExecutionCommand
    get() = termuxSession.executionCommand

  companion object {
    private val log = LoggerFactory.getLogger(IdesetupSession::class.java)
    private val managedSessionHandles = HashSet<String>()

    @JvmStatic
    fun isManagedSession(terminalSession: TerminalSession): Boolean =
      managedSessionHandles.contains(terminalSession.mHandle)

    @JvmStatic
    fun wrap(session: TermuxSession?, script: File): IdesetupSession? {
      if (session == null) return null
      val wrapped = IdesetupSession(session, script)
      managedSessionHandles.add(session.terminalSession.mHandle)
      return wrapped
    }

    @JvmStatic
    fun createScript(context: Context): File? {
      val script = Environment.createTempFile()

      if (!writeIdesetupScript(context, script)) {
        return null
      }

      FileUtils.setFilePermissions("idesetupScript", script.absolutePath, "r-x")

      return script
    }

    private fun writeIdesetupScript(context: Context, script: File): Boolean {
      context.assets.open(ToolsManager.getCommonAsset("idesetup.sh")).use {
        val error = FileUtils.writeTextToFile("idsetupScript", script.absolutePath,
          StandardCharsets.UTF_8, it.readBytes().toString(StandardCharsets.UTF_8), false)
        if (error != null) {
          log.error("Failed to write idesetup script: {}", error.errorLogString)
          return false
        }
      }

      return true
    }
  }

  fun finish() {
    managedSessionHandles.remove(termuxSession.terminalSession.mHandle)
    termuxSession.finish()

    val error = FileUtils.deleteFile("idesetupScript", script.absolutePath, true)
    if (error != null) {
      log.error(error.errorLogString)
    }
  }
}