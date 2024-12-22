/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import com.ttop.app.apex.R

class ATEPreferenceCategory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : PreferenceCategory(context, attrs, defStyleAttr, defStyleRes) {

    init {
        layoutResource = R.layout.custom_preference_category
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val title = holder.itemView.findViewById<TextView>(android.R.id.title)
        title.setTextColor(
            com.ttop.app.apex.libraries.appthemehelper.ThemeStore.accentColor(context)
        )

        when (com.ttop.app.apex.libraries.appthemehelper.ThemeStore.fontSize(context)) {
            "12" -> {
                title.textSize = 14f
            }

            "13" -> {
                title.textSize = 15f
            }

            "14" -> {
                title.textSize = 16f
            }

            "15" -> {
                title.textSize = 17f
            }

            "16" -> {
                title.textSize = 18f
            }

            "17" -> {
                title.textSize = 19f
            }

            "18" -> {
                title.textSize = 20f
            }

            "19" -> {
                title.textSize = 21f
            }

            "20" -> {
                title.textSize = 22f
            }

            "21" -> {
                title.textSize = 23f
            }

            "22" -> {
                title.textSize = 24f
            }

            "23" -> {
                title.textSize = 25f
            }

            "24" -> {
                title.textSize = 26f
            }
        }
    }
}
