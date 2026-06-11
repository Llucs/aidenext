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
package com.aidenext.utils

import android.content.Context
import com.aidenext.actions.ActionItem.Location.EDITOR_FILE_TABS
import com.aidenext.actions.ActionItem.Location.EDITOR_FILE_TREE
import com.aidenext.actions.ActionItem.Location.EDITOR_TOOLBAR
import com.aidenext.actions.ActionsRegistry
import com.aidenext.actions.build.ProjectSyncAction
import com.aidenext.actions.build.QuickRunWithCancellationAction
import com.aidenext.actions.build.RunTasksAction
import com.aidenext.actions.editor.CopyAction
import com.aidenext.actions.editor.CutAction
import com.aidenext.actions.editor.ExpandSelectionAction
import com.aidenext.actions.editor.LongSelectAction
import com.aidenext.actions.editor.PasteAction
import com.aidenext.actions.editor.SelectAllAction
import com.aidenext.actions.etc.DisconnectLogSendersAction
import com.aidenext.actions.etc.FindActionMenu
import com.aidenext.actions.etc.LaunchAppAction
import com.aidenext.actions.etc.PreviewLayoutAction
import com.aidenext.actions.etc.ReloadColorSchemesAction
import com.aidenext.actions.file.CloseAllFilesAction
import com.aidenext.actions.file.CloseFileAction
import com.aidenext.actions.file.CloseOtherFilesAction
import com.aidenext.actions.file.FormatCodeAction
import com.aidenext.actions.file.SaveFileAction
import com.aidenext.actions.filetree.CopyPathAction
import com.aidenext.actions.filetree.DeleteAction
import com.aidenext.actions.filetree.NewFileAction
import com.aidenext.actions.filetree.NewFolderAction
import com.aidenext.actions.filetree.OpenWithAction
import com.aidenext.actions.filetree.RenameAction
import com.aidenext.actions.text.RedoAction
import com.aidenext.actions.text.UndoAction

/**
 * Takes care of registering actions to the actions registry for the editor activity.
 *
 * @author Akash Yadav
 */
class EditorActivityActions {

  companion object {

    @JvmStatic
    fun register(context: Context) {
      clear()
      val registry = ActionsRegistry.getInstance()
      var order = 0

      // Toolbar actions
      registry.registerAction(UndoAction(context, order++))
      registry.registerAction(RedoAction(context, order++))
      registry.registerAction(QuickRunWithCancellationAction(context, order++))
      registry.registerAction(RunTasksAction(context, order++))
      registry.registerAction(SaveFileAction(context, order++))
      registry.registerAction(PreviewLayoutAction(context, order++))
      registry.registerAction(FindActionMenu(context, order++))
      registry.registerAction(ProjectSyncAction(context, order++))
      registry.registerAction(ReloadColorSchemesAction(context, order++))
      registry.registerAction(DisconnectLogSendersAction(context, order++))
      registry.registerAction(LaunchAppAction(context, order++))

      // editor text actions
      registry.registerAction(ExpandSelectionAction(context, order++))
      registry.registerAction(SelectAllAction(context, order++))
      registry.registerAction(LongSelectAction(context, order++))
      registry.registerAction(CutAction(context, order++))
      registry.registerAction(CopyAction(context, order++))
      registry.registerAction(PasteAction(context, order++))
      registry.registerAction(FormatCodeAction(context, order++))

      // file tab actions
      registry.registerAction(CloseFileAction(context, order++))
      registry.registerAction(CloseOtherFilesAction(context, order++))
      registry.registerAction(CloseAllFilesAction(context, order++))

      // file tree actions
      registry.registerAction(CopyPathAction(context, order++))
      registry.registerAction(DeleteAction(context, order++))
      registry.registerAction(NewFileAction(context, order++))
      registry.registerAction(NewFolderAction(context, order++))
      registry.registerAction(OpenWithAction(context, order++))
      registry.registerAction(RenameAction(context, order++))
    }

    @JvmStatic
    fun clear() {
      // EDITOR_TEXT_ACTIONS should not be cleared as the language servers register actions there as
      // well
      val locations = arrayOf(EDITOR_TOOLBAR, EDITOR_FILE_TABS, EDITOR_FILE_TREE)
      val registry = ActionsRegistry.getInstance()
      locations.forEach(registry::clearActions)
    }
  }
}
