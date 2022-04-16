package code.name.monkey.retro.util.theme

import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import code.name.monkey.retro.App
import code.name.monkey.retro.R
import code.name.monkey.retro.extensions.generalThemeValue
import code.name.monkey.retro.util.PreferenceUtil
import code.name.monkey.retro.util.theme.ThemeMode.*

object ThemeManager {

    @StyleRes
    fun getThemeResValue(): Int =
        if (PreferenceUtil.materialYou) {
            R.style.Theme_RetroMusic_MD3
        } else {
            when (App.getContext().generalThemeValue) {
                LIGHT -> R.style.Theme_RetroMusic_Light
                DARK -> R.style.Theme_RetroMusic_Base
                BLACK -> R.style.Theme_RetroMusic_Black
                AUTO -> R.style.Theme_RetroMusic_FollowSystem
            }
        }

    fun getNightMode(): Int = when (App.getContext().generalThemeValue) {
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DARK -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}