package com.ttop.app.apex.util.theme

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.generalThemeValue
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode.AUTO
import com.ttop.app.apex.util.theme.ThemeMode.BLACK
import com.ttop.app.apex.util.theme.ThemeMode.DARK
import com.ttop.app.apex.util.theme.ThemeMode.LIGHT
import com.ttop.app.apex.util.theme.ThemeMode.AUTO_BLACK
import com.ttop.app.apex.util.theme.ThemeMode.MD3

@StyleRes
fun Context.getThemeResValue(): Int =
    when (generalThemeValue) {
     MD3 -> {
            R.style.Theme_Apex_MD3
        }
        LIGHT -> {
            R.style.Theme_Apex_Light
        }

        DARK -> {
            R.style.Theme_Apex_Base
        }

        BLACK -> {
            R.style.Theme_Apex_Black
        }

        AUTO -> {
            R.style.Theme_Apex_FollowSystem
        }
        AUTO_BLACK -> {
            when (applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> R.style.Theme_Apex_Black
                Configuration.UI_MODE_NIGHT_NO,
                Configuration.UI_MODE_NIGHT_UNDEFINED -> R.style.Theme_Apex_Light

                else -> R.style.Theme_Apex_Black
            }
        }
    }

fun Context.getNightMode(): Int = when (generalThemeValue) {
    LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    DARK, BLACK -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}