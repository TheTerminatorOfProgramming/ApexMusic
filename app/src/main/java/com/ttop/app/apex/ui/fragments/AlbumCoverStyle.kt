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
package com.ttop.app.apex.ui.fragments

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ttop.app.apex.R

enum class AlbumCoverStyle(
    @StringRes val titleRes: Int,
    @DrawableRes val drawableResId: Int,
    val id: Int
) {
    Card(R.string.card, R.drawable.player_blur_card, 3),
    Circle(R.string.circular, R.drawable.player_circle, 2),
    Flat(R.string.flat, R.drawable.player_flat, 1),
    FullCard(R.string.full_card, R.drawable.player_adaptive, 5),
    Full(R.string.full, R.drawable.player_full, 4),
    Normal(R.string.normal, R.drawable.player_normal, 0),
    Peek(R.string.peek, R.drawable.album_cover_peek, 6)
}
