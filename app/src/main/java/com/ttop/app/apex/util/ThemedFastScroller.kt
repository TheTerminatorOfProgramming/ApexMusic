/*
 * Copyright (c) 2020 Hemanth Savarala.
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
package com.ttop.app.apex.util

import android.view.ViewGroup
import com.ttop.app.apex.views.PopupBackground
import com.ttop.app.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.appthemehelper.util.ColorUtil.isColorLight
import com.ttop.app.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import com.ttop.app.appthemehelper.util.TintHelper
import com.ttop.app.fastscroller.FastScroller
import com.ttop.app.fastscroller.FastScrollerBuilder
import com.ttop.app.fastscroller.PopupStyles
import com.ttop.app.fastscroller.R

object ThemedFastScroller {
    fun create(view: ViewGroup, autoHide: Boolean): FastScroller {
        val context = view.context
        val color = accentColor(context)
        val textColor = getPrimaryTextColor(context, isColorLight(color))
        val fastScrollerBuilder = FastScrollerBuilder(view)
        fastScrollerBuilder.useMd2Style()
        fastScrollerBuilder.setPopupStyle { popupText ->
            PopupStyles.MD2.accept(popupText)
            popupText.background = PopupBackground(context, color)
            popupText.setTextColor(textColor)
        }

        fastScrollerBuilder.setThumbDrawable(
            TintHelper.createTintedDrawable(
                context,
                R.drawable.afs_md2_thumb,
                color
            )
        )

        if (!autoHide) {
            fastScrollerBuilder.disableScrollbarAutoHide()
        }

        return fastScrollerBuilder.build()
    }
}