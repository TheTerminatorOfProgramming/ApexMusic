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
import android.content.res.Configuration
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.afollestad.materialdialogs.color.colorChooser
import com.ttop.app.apex.*
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.extensions.installLanguageAndRecreate
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.*
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ACCENT_COLORS
import com.ttop.app.appthemehelper.ACCENT_COLORS_SUB
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEColorPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.VersionUtils

/**
 * @author Hemanth S (h4h13).
 */

class ThemeSettingsFragment : AbsSettingsFragment() {
    @SuppressLint("CheckResult")
    override fun invalidateSettings() {
        val generalTheme: Preference? = findPreference(GENERAL_THEME)
        generalTheme?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                ThemeStore.markChanged(requireContext())

                if (VersionUtils.hasOreo()) {
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                }
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
                    if (VersionUtils.hasOreo())
                        DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                    restartActivity()
                }
                neutralButton(res = R.string.reset_action) {
                    if (BuildConfig.DEBUG) {
                        ThemeStore.editTheme(requireContext()).accentColor(resources.getColor(R.color.default_debug_color)).commit()
                    }else {
                        ThemeStore.editTheme(requireContext()).accentColor(resources.getColor(R.color.default_color)).commit()
                    }
                    if (VersionUtils.hasOreo())
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
            if (VersionUtils.hasOreo()) {
                requireActivity().setTheme(PreferenceUtil.themeResFromPrefValue("black"))
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            }
            restartActivity()
            true
        }

        val desaturatedColor: ATESwitchPreference? = findPreference(DESATURATED_COLOR)
        desaturatedColor?.setOnPreferenceChangeListener { _, value ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val desaturated = value as Boolean
            ThemeStore.prefs(requireContext()).edit {
                putBoolean(DESATURATED_COLOR, desaturated)
            }
            PreferenceUtil.isDesaturatedColor = desaturated
            restartActivity()
            true
        }

        val colorAppShortcuts: TwoStatePreference? = findPreference(SHOULD_COLOR_APP_SHORTCUTS)
        if (!VersionUtils.hasOreo()) {
            colorAppShortcuts?.isVisible = false
        } else {
            colorAppShortcuts?.isChecked = PreferenceUtil.isColoredAppShortcuts
            colorAppShortcuts?.setOnPreferenceChangeListener { _, newValue ->
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                PreferenceUtil.isColoredAppShortcuts = newValue as Boolean
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                true
            }
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


        val customFontBold: TwoStatePreference? = findPreference(CUSTOM_FONT_BOLD)
        customFontBold?.isChecked = PreferenceUtil.isCustomFontBold
        customFontBold?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            PreferenceUtil.shouldRecreate = true
            restartActivity()
            true
        }

        val customFont: Preference? = findPreference(CUSTOM_FONT)
        customFont?.setOnPreferenceChangeListener { _, newValue ->
            when (newValue as String){
                "default", "drexs", "hermanoalto", "nothing", "pencil", "binjay", "hiatus", "apex", "neue" -> {
                    customFontBold?.isChecked = false
                    customFontBold?.isEnabled = false
                }
                "barlow", "jura", "caviar"  -> {
                    customFontBold?.isEnabled = true
                }
            }
            PreferenceUtil.shouldRecreate = true
            restartActivity()
            true
        }

        val extraControls: TwoStatePreference? = findPreference(TOGGLE_ADD_CONTROLS)
        extraControls?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val progressbar: TwoStatePreference? = findPreference(PROGRESS_BAR_STYLE)
        progressbar?.isChecked = PreferenceUtil.progressBarStyle
        progressbar?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val progressbarAlignment: TwoStatePreference? = findPreference(PROGRESS_BAR_ALIGNMENT)
        progressbarAlignment?.isChecked = PreferenceUtil.progressBarAlignment
        progressbarAlignment?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val extendedAccent: TwoStatePreference? = findPreference(EXTENDED_ACCENT)
        extendedAccent?.isChecked = PreferenceUtil.isExtendedAccent
        extendedAccent?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val dismissCheck: TwoStatePreference? = findPreference(DISMISS_FAILSAFE)
        dismissCheck?.isChecked = PreferenceUtil.isDismissFailsafe
        dismissCheck?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val dismissMethod: Preference? = findPreference(DISMISS_METHOD)
        dismissMethod?.setOnPreferenceChangeListener { _, newValue ->
            PreferenceUtil.dismissMethod = newValue as String

            dismissCheck?.isEnabled = PreferenceUtil.dismissMethod == "long_touch"
            true
        }

        val languagePreference: ATEListPreference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            return@setOnPreferenceChangeListener true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languagePreference: Preference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            if (newValue as? String == "auto") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } else {
                // Install the languages from Play Store first and then set the application locale
                requireActivity().installLanguageAndRecreate(newValue.toString()) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(
                            newValue as? String
                        )
                    )
                }
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (VersionUtils.hasR()) {
            if (ApexUtil.isFoldable(requireContext())) {
                addPreferencesFromResource(R.xml.pref_general_foldable)
            }else if (!ApexUtil.isFoldable(requireContext()) && ApexUtil.isTablet){
                addPreferencesFromResource(R.xml.pref_general_tablet)
            }else {
                addPreferencesFromResource(R.xml.pref_general)
            }
        }else {
            addPreferencesFromResource(R.xml.pref_general)
        }

        val customFontBold: TwoStatePreference? = findPreference(CUSTOM_FONT_BOLD)
        when (PreferenceUtil.isCustomFont){
            "default", "drexs", "hermanoalto", "nothing", "pencil", "binjay", "hiatus", "apex", "neue" -> {
                customFontBold?.isChecked = false
                customFontBold?.isEnabled = false
            }
            "barlow", "jura", "caviar"  -> {
                customFontBold?.isEnabled = true
            }
        }

        val wallpaperAccent: ATESwitchPreference? = findPreference(WALLPAPER_ACCENT)
        wallpaperAccent?.isVisible = VersionUtils.hasOreoMR1() && !VersionUtils.hasS()

        val dismissCheck: TwoStatePreference? = findPreference(DISMISS_FAILSAFE)
        dismissCheck?.isEnabled = PreferenceUtil.dismissMethod == "long_touch"

        val blackTheme: ATESwitchPreference? = findPreference(BLACK_THEME)
        if (PreferenceUtil.baseTheme == "auto") {
            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    blackTheme?.isEnabled = true
                }
                Configuration.UI_MODE_NIGHT_NO,
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    blackTheme?.isEnabled = false
                }
            }
        }

        if (PreferenceUtil.baseTheme == "dark") {
            blackTheme?.isEnabled = true
        }

        if (PreferenceUtil.baseTheme == "light") {
            blackTheme?.isEnabled = false
        }
    }
}