package code.name.monkey.retro.model.smartplaylist

import androidx.annotation.DrawableRes
import code.name.monkey.retro.R
import code.name.monkey.retro.model.AbsCustomPlaylist

abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)