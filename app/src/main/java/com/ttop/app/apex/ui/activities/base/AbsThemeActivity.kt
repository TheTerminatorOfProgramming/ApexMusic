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
package com.ttop.app.apex.ui.activities.base

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.os.LocaleListCompat
import com.ttop.app.apex.LANGUAGE_NAME
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.exitFullscreen
import com.ttop.app.apex.extensions.hideStatusBar
import com.ttop.app.apex.extensions.installSplitCompat
import com.ttop.app.apex.extensions.maybeSetScreenOn
import com.ttop.app.apex.extensions.setDrawBehindSystemBars
import com.ttop.app.apex.extensions.setImmersiveFullscreen
import com.ttop.app.apex.extensions.setLightStatusBarAuto
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.getNightMode
import com.ttop.app.apex.util.theme.getThemeResValue

abstract class AbsThemeActivity : ATHToolbarActivity(), Runnable,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        updateLocale()
        updateTheme()
        hideStatusBar()
        super.onCreate(savedInstanceState)
        setDrawBehindSystemBars()
        maybeSetScreenOn()
        setLightStatusBarAuto(surfaceColor())

        window.decorView.isForceDarkAllowed = false

        if (!ApexUtil.isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            LANGUAGE_NAME -> {
                //updateLocale()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
    }

    private fun updateTheme() {
        setTheme(getThemeResValue())
        if (PreferenceUtil.materialYou) {
            setDefaultNightMode(getNightMode())
        }

        if (PreferenceUtil.isApexFont) {
            setTheme(R.style.ApexThemeOverlay)
        }

        setTheme(R.style.CircleFABOverlay)

        when (PreferenceUtil.fontSize) {
            "12" -> {
                setTheme(R.style.FontSize12)
            }

            "13" -> {
                setTheme(R.style.FontSize13)
            }

            "14" -> {
                setTheme(R.style.FontSize14)
            }

            "15" -> {
                setTheme(R.style.FontSize15)
            }

            "16" -> {
                setTheme(R.style.FontSize16)
            }

            "17" -> {
                setTheme(R.style.FontSize17)
            }

            "18" -> {
                setTheme(R.style.FontSize18)
            }

            "19" -> {
                setTheme(R.style.FontSize19)
            }

            "20" -> {
                setTheme(R.style.FontSize20)
            }

            "21" -> {
                setTheme(R.style.FontSize21)
            }

            "22" -> {
                setTheme(R.style.FontSize22)
            }

            "23" -> {
                setTheme(R.style.FontSize23)
            }

            "24" -> {
                setTheme(R.style.FontSize24)
            }
        }
    }

    private fun updateLocale() {
        val localeCode = PreferenceUtil.languageCode
        if (PreferenceUtil.isLocaleAutoStorageEnabled) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
            PreferenceUtil.isLocaleAutoStorageEnabled = true
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideStatusBar()
            handler.removeCallbacks(this)
            handler.postDelayed(this, 300)
        } else {
            handler.removeCallbacks(this)
        }
    }

    override fun run() {
        setImmersiveFullscreen()
    }

    override fun onStop() {
        handler.removeCallbacks(this)
        super.onStop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
        exitFullscreen()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            handler.removeCallbacks(this)
            handler.postDelayed(this, 500)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        installSplitCompat()
    }
}
