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

package com.ttop.app.apex.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.navigationrail.NavigationRailView
import com.ttop.app.apex.extensions.addAlpha
import com.ttop.app.apex.extensions.setItemColors
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.PreferenceUtil

class TintedNavigationRailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : NavigationRailView(context, attrs, defStyleAttr) {

    init {
        if (!isInEditMode) {
            labelVisibilityMode = PreferenceUtil.tabTitleMode

            val accentColor = ThemeStore.accentColor(context)
            val alternateColor = if (PreferenceUtil.materialYou) {
                ContextCompat.getColor(context, com.ttop.app.apex.R.color.m3_widget_other_text)
            } else {
                ColorUtil.getAnalogousColor(accentColor)[1].toArgb()
            }

            val iconColor = ATHColorUtil.lightenColor(accentColor, 0.1f)

            setItemColors(iconColor, alternateColor)
            itemRippleColor = ColorStateList.valueOf(alternateColor.addAlpha(0.08F))
            itemActiveIndicatorColor = ColorStateList.valueOf(alternateColor.addAlpha(0.12F))
        }
    }

    override fun getMaxItemCount(): Int {
        return 7
    }
}