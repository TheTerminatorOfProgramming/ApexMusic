/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ttop.app.apex.appshortcuts

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import androidx.core.content.getSystemService
import com.ttop.app.apex.appshortcuts.shortcuttype.LastAddedShortcutType
import com.ttop.app.apex.appshortcuts.shortcuttype.ShuffleAllShortcutType
import com.ttop.app.apex.appshortcuts.shortcuttype.TopTracksShortcutType

class DynamicShortcutManager(private val context: Context) {
    private val shortcutManager: ShortcutManager? =
        this.context.getSystemService()

    private val defaultShortcuts: List<ShortcutInfo>
        get() = listOf(
            ShuffleAllShortcutType(context).shortcutInfo,
            TopTracksShortcutType(context).shortcutInfo,
            LastAddedShortcutType(context).shortcutInfo
        )

    fun initDynamicShortcuts() {
        // if (shortcutManager.dynamicShortcuts.size == 0) {
        shortcutManager?.dynamicShortcuts = defaultShortcuts
        // }
    }

    fun updateDynamicShortcuts() {
        shortcutManager?.updateShortcuts(defaultShortcuts)
    }

    companion object {
        fun reportShortcutUsed(context: Context, shortcutId: String) {
            context.getSystemService<ShortcutManager>()?.reportShortcutUsed(shortcutId)
        }
    }
}
