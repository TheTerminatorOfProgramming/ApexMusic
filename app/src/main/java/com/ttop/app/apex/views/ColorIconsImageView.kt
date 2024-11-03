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
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.google.android.material.color.MaterialColors
import com.ttop.app.apex.R
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil
import com.ttop.app.apex.util.ApexColorUtil
import com.ttop.app.apex.util.PreferenceUtil


class ColorIconsImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppCompatImageView(context, attrs, defStyleAttr) {


    init {
        // Load the styled attributes and set their properties
        context.withStyledAttributes(attrs, R.styleable.ColorIconsImageView, 0, 0) {
            val color = getColor(R.styleable.ColorIconsImageView_iconBackgroundColor, Color.RED)
            setIconBackgroundColor(color)
        }
    }

    fun setIconBackgroundColor(color: Int) {
        background = ContextCompat.getDrawable(context, R.drawable.color_circle_gradient)
        if (ATHUtil.isWindowBackgroundDark(context) && PreferenceUtil.isDesaturatedColor) {
            val desaturatedColor = ApexColorUtil.desaturateColor(color, 0.4f)
            backgroundTintList = ColorStateList.valueOf(desaturatedColor)
            imageTintList =
                ColorStateList.valueOf(ATHUtil.resolveColor(context, R.attr.colorSurface))
        } else {
            val finalColor = MaterialColors.harmonize(
                color,
                ThemeStore.accentColor(context)
            )
            backgroundTintList = ColorStateList.valueOf(ATHColorUtil.adjustAlpha(finalColor, 0.22f))
            imageTintList = ColorStateList.valueOf(ATHColorUtil.withAlpha(finalColor, 0.75f))
        }
        requestLayout()
        invalidate()
    }
}
