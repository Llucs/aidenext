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

import com.aidenext.activities.TerminalActivity
import com.termux.app.terminal.TermuxTerminalSessionActivityClient
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

class IdeTerminalSessionClient(
  activity: TerminalActivity
) : TermuxTerminalSessionActivityClient(activity) {
  private val mActivityRef = activity

  override fun onSessionFinished(finishedSession: TerminalSession) {
    val termuxSession = mActivityRef.termuxService?.getTermuxSessionForTerminalSession(
      finishedSession)
    if (termuxSession != null && IdesetupSession.isManagedSession(finishedSession)) {
      mActivityRef.setResult(finishedSession.exitStatus)
    }

    super.onSessionFinished(finishedSession)
  }
}