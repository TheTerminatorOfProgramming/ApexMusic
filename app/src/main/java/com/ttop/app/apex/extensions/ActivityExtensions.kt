/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as publish`ed by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ttop.app.apex.extensions

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper

fun AppCompatActivity.applyToolbar(toolbar: MaterialToolbar) {
    ToolbarContentTintHelper.colorBackButton(toolbar)
    setSupportActionBar(toolbar)
}

inline fun <reified T : Any> Activity.extra(key: String, default: T? = null) = lazy {
    val value = intent?.extras?.get(key)
    if (value is T) value else default
}

inline fun <reified T : Any> Intent.extra(key: String, default: T? = null) = lazy {
    val value = extras?.get(key)
    if (value is T) value else default
}

inline fun <reified T : Any> Activity.extraNotNull(key: String, default: T? = null) = lazy {
    val value = intent?.extras?.get(key)
    requireNotNull(if (value is T) value else default) { key }
}

fun Activity.dip(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

inline val Activity.rootView: View get() = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)