/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.extensions

import android.content.Intent
import android.support.annotation.DimenRes
import android.support.v4.app.Fragment

inline fun <reified T> Fragment.startActivity(func: Intent.() -> Intent) = startActivity(Intent(activity, T::class.java).func())

inline fun <reified T> Fragment.startActivity() = startActivity(Intent(activity, T::class.java))

fun Fragment.getPxSize(@DimenRes id: Int) = resources.getDimensionPixelSize(id)