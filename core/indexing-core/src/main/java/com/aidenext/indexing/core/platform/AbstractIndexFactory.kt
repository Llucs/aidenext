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

package com.aidenext.indexing.core.platform

import com.aidenext.indexing.IIndexFactory
import com.aidenext.indexing.IIndexParams
import com.aidenext.indexing.IIndexable

/**
 * Abstract implementation of the [IIndexFactory].
 *
 * @author Akash Yadav
 */
abstract class AbstractIndexFactory<I : IIndexable, P : IIndexParams> : IIndexFactory<I, P> {

  override var params: P? = null
}