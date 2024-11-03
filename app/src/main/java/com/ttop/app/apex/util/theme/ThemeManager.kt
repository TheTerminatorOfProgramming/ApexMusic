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

@StyleRes
fun Context.getThemeResValue(): Int =
    when (generalThemeValue) {
        LIGHT -> {
            if (PreferenceUtil.materialYou) {
                R.style.Theme_Apex_MD3_Light
            } else {
                R.style.Theme_Apex_Light
            }
        }

        DARK -> {
            if (PreferenceUtil.materialYou) {
                R.style.Theme_Apex_MD3_Dark
            } else {
                R.style.Theme_Apex_Base
            }
        }

        BLACK -> {
            if (PreferenceUtil.materialYou) {
                R.style.Theme_Apex_MD3_Black
            } else {
                R.style.Theme_Apex_Black
            }
        }

        AUTO -> {
            if (PreferenceUtil.materialYou) {
                when (applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> R.style.Theme_Apex_MD3_Dark
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> R.style.Theme_Apex_MD3_Light

                    else -> R.style.Theme_Apex_MD3_Dark
                }
            } else {
                R.style.Theme_Apex_FollowSystem
            }
        }
    }

fun Context.getNightMode(): Int = when (generalThemeValue) {
    LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    DARK -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}