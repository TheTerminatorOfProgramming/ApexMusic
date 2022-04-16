package com.ttop.app.apex.model.smartplaylist

import androidx.annotation.DrawableRes
import com.ttop.app.apex.R
import com.ttop.app.apex.model.AbsCustomPlaylist

abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)