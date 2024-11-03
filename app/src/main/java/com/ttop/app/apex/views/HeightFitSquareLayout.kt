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
import android.util.AttributeSet
import android.widget.FrameLayout

class HeightFitSquareLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var forceSquare = true

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var i = widthMeasureSpec
        if (forceSquare) {
            i = heightMeasureSpec
        }
        super.onMeasure(i, heightMeasureSpec)
    }
}