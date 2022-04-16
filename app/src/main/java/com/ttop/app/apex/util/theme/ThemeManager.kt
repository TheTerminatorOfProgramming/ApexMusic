package com.ttop.app.apex.util.theme

import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.ttop.app.apex.App
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.generalThemeValue
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode.*

object ThemeManager {

    @StyleRes
    fun getThemeResValue(): Int =
        if (PreferenceUtil.materialYou) {
            R.style.Theme_Apex_MD3
        } else {
            when (App.getContext().generalThemeValue) {
                LIGHT -> R.style.Theme_Apex_Light
                DARK -> R.style.Theme_Apex_Base
                BLACK -> R.style.Theme_Apex_Black
                AUTO -> R.style.Theme_Apex_FollowSystem
            }
        }

    fun getNightMode(): Int = when (App.getContext().generalThemeValue) {
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DARK -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}