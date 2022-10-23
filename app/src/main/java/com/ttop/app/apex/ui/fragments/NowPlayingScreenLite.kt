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

enum class NowPlayingScreenLite constructor(
    @param:StringRes @field:StringRes
    val titleRes: Int,
    @param:DrawableRes @field:DrawableRes val drawableResId: Int,
    val id: Int,
    val defaultCoverTheme: AlbumCoverStyle?
) {
    // Some Now playing themes look better with particular Album cover theme

    Classic(R.string.classic, R.drawable.player_classic, 16, AlbumCoverStyle.Full),
    Flat(R.string.flat, R.drawable.player_flat, 1, AlbumCoverStyle.Flat),
    MD3(R.string.md3, R.drawable.player_md3, 18, AlbumCoverStyle.Normal),
    Normal(R.string.normal, R.drawable.player_normal, 0, AlbumCoverStyle.Normal),
    Peek(R.string.peek, R.drawable.player_peek, 14, AlbumCoverStyle.Normal),
    Simple(R.string.simple, R.drawable.player_simple, 8, AlbumCoverStyle.Normal),
    Swipe(R.string.swipe, R.drawable.player_swipe, 19, AlbumCoverStyle.Full),
}
