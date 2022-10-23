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
package com.ttop.app.apex.ui.fragments.settings

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.afollestad.materialdialogs.color.colorChooser
import com.google.android.material.color.DynamicColors
import com.ttop.app.apex.*
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.appwidgets.AppWidgetBig
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetFullCircle
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.*
import com.ttop.app.apex.ui.fragments.NowPlayingScreenLite
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ACCENT_COLORS
import com.ttop.app.appthemehelper.ACCENT_COLORS_SUB
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEColorPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.VersionUtils

/**
 * @author Hemanth S (h4h13).
 */

class ThemeSettingsFragment : AbsSettingsFragment() {
    @SuppressLint("CheckResult")
    override fun invalidateSettings() {
        val uiMode: Preference? = findPreference(UI_MODE)
        uiMode?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                val packageManager: PackageManager? = context?.packageManager
                if (newValue == "full") {
                    context?.let {
                        ComponentName(
                            it,
                            AppWidgetBig::class.java
                        )
                    }?.let {
                        packageManager!!.setComponentEnabledSetting(
                            it,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }

                    context?.let {
                        ComponentName(
                            it,
                            AppWidgetCircle::class.java
                        )
                    }?.let {
                        packageManager!!.setComponentEnabledSetting(
                            it,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }

                    context?.let {
                        ComponentName(
                            it,
                            AppWidgetFullCircle::class.java
                        )
                    }?.let {
                        packageManager!!.setComponentEnabledSetting(
                            it,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }else {
                    context?.let {
                        ComponentName(
                            it,
                            AppWidgetBig::class.java
                        )
                    }?.let {
                        packageManager!!.setComponentEnabledSetting(
                            it,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }

                    context?.let {
                        ComponentName(
                            it,
                            AppWidgetCircle::class.java
                        )
                    }?.let {
                        packageManager!!.setComponentEnabledSetting(
                            it,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }

                    context?.let {
                        ComponentName(
                            it,
                            AppWidgetFullCircle::class.java
                        )
                    }?.let {
                        packageManager!!.setComponentEnabledSetting(
                            it,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }
                restartActivity()
                true
            }
        }

        val generalTheme: Preference? = findPreference(GENERAL_THEME)
        generalTheme?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                ThemeStore.markChanged(requireContext())

                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                restartActivity()
                true
            }
        }

        val accentColorPref: ATEColorPreference? = findPreference(ACCENT_COLOR)
        val accentColor = ThemeStore.accentColor(requireContext())
        accentColorPref?.setColor(accentColor, ColorUtil.darkenColor(accentColor))
        accentColorPref?.setOnPreferenceClickListener {
            materialDialog().show {
                colorChooser(
                    initialSelection = accentColor,
                    showAlphaSelector = false,
                    colors = ACCENT_COLORS,
                    subColors = ACCENT_COLORS_SUB, allowCustomArgb = true
                ) { _, color ->
                    ThemeStore.editTheme(requireContext()).accentColor(color).commit()
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                    restartActivity()
                }
            }
            return@setOnPreferenceClickListener true
        }

        val blackTheme: ATESwitchPreference? = findPreference(BLACK_THEME)
        blackTheme?.setOnPreferenceChangeListener { _, _ ->
            ThemeStore.markChanged(requireContext())
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            requireActivity().setTheme(PreferenceUtil.themeResFromPrefValue("black"))
            DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            restartActivity()
            true
        }

        val desaturatedColor: ATESwitchPreference? = findPreference(DESATURATED_COLOR)
        desaturatedColor?.setOnPreferenceChangeListener { _, value ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val desaturated = value as Boolean
            ThemeStore.prefs(requireContext()).edit {
                putBoolean("desaturated_color", desaturated)
            }
            PreferenceUtil.isDesaturatedColor = desaturated
            restartActivity()
            true
        }

        val colorAppShortcuts: TwoStatePreference? = findPreference(SHOULD_COLOR_APP_SHORTCUTS)
        colorAppShortcuts?.isChecked = PreferenceUtil.isColoredAppShortcuts
        colorAppShortcuts?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            PreferenceUtil.isColoredAppShortcuts = newValue as Boolean
            DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            true
        }

        val materialYou: ATESwitchPreference? = findPreference(MATERIAL_YOU)
        materialYou?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (newValue as Boolean) {
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            }
            restartActivity()
            PreferenceUtil.shouldRecreate = true
            true
        }

        val wallpaperAccent: ATESwitchPreference? = findPreference(WALLPAPER_ACCENT)
        wallpaperAccent?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            restartActivity()
            true
        }

        val customFont: Preference? = findPreference(CUSTOM_FONT)
        customFont?.setOnPreferenceChangeListener { _, _ ->

            PreferenceUtil.shouldRecreate = true
            restartActivity()
            true
        }

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.isEnabled =
            if (PreferenceUtil.isUiMode == "full") {
                PreferenceUtil.nowPlayingScreen in listOf(Normal, Material, Flat)
            }else {
                PreferenceUtil.nowPlayingScreenLite in listOf(NowPlayingScreenLite.Normal, NowPlayingScreenLite.Flat)
            }
        adaptiveColor?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val swipeGestures: TwoStatePreference? = findPreference(TOGGLE_MINI_SWIPE)
        swipeGestures?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val autoplay: TwoStatePreference? = findPreference(TOGGLE_AUTOPLAY)
        autoplay?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val swipeDismiss: TwoStatePreference? = findPreference(SWIPE_DOWN_DISMISS)
        swipeDismiss?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }


        val extraControls: TwoStatePreference? = findPreference(TOGGLE_ADD_CONTROLS)
        extraControls?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val volume: TwoStatePreference? = findPreference(TOGGLE_VOLUME)
        volume?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val progressbar: TwoStatePreference? = findPreference(PROGRESS_BAR_STYLE)
        progressbar?.isChecked = PreferenceUtil.progressBarStyle
        progressbar?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val progressbarAlignment: TwoStatePreference? = findPreference(PROGRESS_BAR_ALIGNMENT)
        progressbarAlignment?.isChecked = PreferenceUtil.progressBarAlignment
        progressbarAlignment?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (PreferenceUtil.isUiMode == "full") {
            addPreferencesFromResource(R.xml.pref_general)
        }else {
            addPreferencesFromResource(R.xml.pref_general_lite)
        }
    }
}