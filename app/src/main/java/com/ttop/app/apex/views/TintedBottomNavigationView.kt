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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ttop.app.apex.extensions.addAlpha
import com.ttop.app.apex.extensions.setItemColors
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.ATHUtil
import dev.chrisbanes.insetter.applyInsetter

class TintedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BottomNavigationView(context, attrs, defStyleAttr) {

    init {
        if (!isInEditMode) {
            applyInsetter {
                type(navigationBars = true) {
                    padding(vertical = true)
                    margin(horizontal = true)
                }
            }

            labelVisibilityMode = PreferenceUtil.tabTitleMode

            val iconColor = ATHUtil.resolveColor(context, android.R.attr.colorControlNormal)
            val accentColor = ThemeStore.accentColor(context)
            setItemColors(iconColor, accentColor)
            itemRippleColor = ColorStateList.valueOf(accentColor.addAlpha(0.08F))
            itemActiveIndicatorColor = ColorStateList.valueOf(accentColor.addAlpha(0.12F))
        }
    }

    override fun getMaxItemCount(): Int {
        return 7
    }
}