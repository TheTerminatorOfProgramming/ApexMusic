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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.os.LocaleListCompat
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.getNightMode
import com.ttop.app.apex.util.theme.getThemeResValue
import com.ttop.app.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.appthemehelper.util.VersionUtils

abstract class AbsThemeActivity : ATHToolbarActivity(), Runnable {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        updateLocale()
        updateTheme()
        hideStatusBar()
        super.onCreate(savedInstanceState)
        setEdgeToEdgeOrImmersive()
        maybeSetScreenOn()
        setLightNavigationBarAuto()
        setLightStatusBarAuto(surfaceColor())
        if (VersionUtils.hasQ()) {
            window.decorView.isForceDarkAllowed = false
        }
    }

    private fun updateTheme() {
        setTheme(getThemeResValue())
        if (PreferenceUtil.materialYou) {
            setDefaultNightMode(getNightMode())
        }

        if (PreferenceUtil.isCustomFont == "sans") {
           if (PreferenceUtil.isCustomFontBold) {
               setTheme(R.style.SansBoldThemeOverlay)
           } else {
               setTheme(R.style.SansThemeOverlay)
           }
        }

        if (PreferenceUtil.isCustomFont == "barlow") {
            if (PreferenceUtil.isCustomFontBold) {
                setTheme(R.style.BarlowBoldThemeOverlay)
            } else {
                setTheme(R.style.BarlowThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "jura") {
            if (PreferenceUtil.isCustomFontBold) {
                setTheme(R.style.JuraBoldThemeOverlay)
            } else {
                setTheme(R.style.JuraThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
            setTheme(R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "samsung") {
            if (PreferenceUtil.isCustomFontBold) {
                setTheme(R.style.SamsungBoldThemeOverlay)
            } else {
                setTheme(R.style.SamsungThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "drexs") {
            setTheme(R.style.DrexsThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hermanoalto") {
            setTheme(R.style.HermanoaltoThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "oneui") {
            if (PreferenceUtil.isCustomFontBold) {
                setTheme(R.style.OneUiBoldThemeOverlay)
            } else {
                setTheme(R.style.OneUiThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "nothing") {
            setTheme(R.style.NothingThemeOverlay)
        }

        if (PreferenceUtil.circlePlayButton) {
            setTheme(R.style.CircleFABOverlay)
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
