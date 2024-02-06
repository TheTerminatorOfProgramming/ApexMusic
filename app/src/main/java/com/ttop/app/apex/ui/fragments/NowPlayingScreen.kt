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

enum class NowPlayingScreen constructor(
    @param:StringRes @field:StringRes
    val titleRes: Int,
    @param:DrawableRes @field:DrawableRes val drawableResId: Int,
    val id: Int,
    val defaultCoverTheme: AlbumCoverStyle?
) {
    // Some Now playing themes look better with particular Album cover theme

    Adaptive(R.string.adaptive, R.drawable.player_adaptive, 0, AlbumCoverStyle.FullCard),
    Blur(R.string.blur, R.drawable.player_blur, 1, AlbumCoverStyle.Normal),
    Card(R.string.card, R.drawable.player_card, 2, AlbumCoverStyle.Full),
    Classic(R.string.classic, R.drawable.player_classic, 3, AlbumCoverStyle.Normal),
    Gradient(R.string.gradient, R.drawable.player_gradient, 4, AlbumCoverStyle.Full),
    Minimal(R.string.minimal, R.drawable.player_minimal, 6, null),
    Peek(R.string.peek, R.drawable.player_peek, 5, AlbumCoverStyle.Full),
}
