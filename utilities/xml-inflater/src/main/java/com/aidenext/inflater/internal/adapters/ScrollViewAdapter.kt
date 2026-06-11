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

package com.aidenext.inflater.internal.adapters

import android.widget.ScrollView
import com.aidenext.annotations.uidesigner.IncludeInDesigner
import com.aidenext.annotations.uidesigner.IncludeInDesigner.Group.LAYOUTS
import com.aidenext.inflater.AttributeHandlerScope
import com.aidenext.inflater.IView
import com.aidenext.inflater.IViewGroup
import com.aidenext.inflater.models.UiWidget
import com.aidenext.resources.R

/**
 * View adapter for [ScrollView].
 *
 * @author Akash Yadav
 */
@com.aidenext.annotations.inflater.ViewAdapter(
  forView = ScrollView::class)
@IncludeInDesigner(group = LAYOUTS)
open class ScrollViewAdapter<T : ScrollView> : FrameLayoutAdapter<T>() {

  override fun createAttrHandlers(
    create: (String, AttributeHandlerScope<T>.() -> Unit) -> Unit
  ) {
    super.createAttrHandlers(create)
    create("fillViewPort") { view.isFillViewport = parseBoolean(value, false) }
  }

  override fun createUiWidgets(): List<UiWidget> {
    return listOf(UiWidget(ScrollView::class.java, R.string.widget_scrollview,
      R.drawable.ic_widget_scroll_view))
  }

  override fun canAcceptChild(view: IViewGroup, child: IView?, name: String
  ): Boolean {
    // scrollview can have only one child
    return view.childCount == 0
  }
}
