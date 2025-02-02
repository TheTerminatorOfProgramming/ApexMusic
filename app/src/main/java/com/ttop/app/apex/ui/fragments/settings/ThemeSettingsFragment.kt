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
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.color.colorChooser
import com.ttop.app.apex.ACCENT_COLOR
import com.ttop.app.apex.BLACK_THEME
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.DESATURATED_COLOR
import com.ttop.app.apex.FONT_SIZE
import com.ttop.app.apex.GENERAL_THEME
import com.ttop.app.apex.LANGUAGE_NAME
import com.ttop.app.apex.R
import com.ttop.app.apex.TOGGLE_ADD_CONTROLS
import com.ttop.app.apex.TOGGLE_MINI_SWIPE_NON_FOLDABLE
import com.ttop.app.apex.TRANSPARENT_MINI_PLAYER
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.installLanguageAndRecreate
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.libraries.appthemehelper.ACCENT_COLORS
import com.ttop.app.apex.libraries.appthemehelper.ACCENT_COLORS_SUB
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEColorPreference
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode

/**
 * @author Hemanth S (h4h13).
 */

class ThemeSettingsFragment : AbsSettingsFragment() {
    @SuppressLint("CheckResult")
    override fun invalidateSettings() {
        val accentColorPref: ATEColorPreference? = findPreference(ACCENT_COLOR)
        val desaturatedColor: ATESwitchPreference? = findPreference(DESATURATED_COLOR)

        val generalTheme: Preference? = findPreference(GENERAL_THEME)
        generalTheme?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                ThemeStore.markChanged(requireContext())
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()

                val colorCategory: Preference? = findPreference("category_color")
                if (newValue != getString(R.string.material_you)) {
                    ThemeStore.prefs(requireContext()).edit {
                        putBoolean(DESATURATED_COLOR, PreferenceUtil.isDesaturatedColor)
                    }
                    accentColorPref?.isEnabled = true
                    desaturatedColor?.isEnabled = true
                    colorCategory?.isVisible = true
                }else {
                    accentColorPref?.isEnabled = false
                    desaturatedColor?.isEnabled = false
                    colorCategory?.isVisible = false
                }

                restartActivity()
                true
            }
        }

        val accentColor = ThemeStore.accentColor(requireContext())
        accentColorPref?.setColor(accentColor, ATHColorUtil.darkenColor(accentColor))
        val hexColor = String.format("#%06X", (0xFFFFFF and accentColor))
        setSummary(accentColorPref!!, hexColor)
        accentColorPref.setOnPreferenceClickListener {
            materialDialog().show {
                colorChooser(
                    initialSelection = accentColor,
                    showAlphaSelector = false,
                    colors = ACCENT_COLORS,
                    subColors = ACCENT_COLORS_SUB,
                    allowCustomArgb = true
                ) { _, color ->
                    ThemeStore.editTheme(requireContext()).accentColor(color).commit()
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                    setSummary(accentColorPref, hexColor)
                    restartActivity()
                }
                negativeButton(res = R.string.reset_action) {
                    if (BuildConfig.DEBUG) {
                        ThemeStore.editTheme(requireContext()).accentColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.default_debug_color
                            )
                        ).commit()
                    } else {
                        ThemeStore.editTheme(requireContext()).accentColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.default_color
                            )
                        ).commit()
                    }
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                    restartActivity()
                }
                getActionButton(WhichButton.POSITIVE).updateTextColor(accentColor())
                getActionButton(WhichButton.NEGATIVE).updateTextColor(accentColor())
            }
            return@setOnPreferenceClickListener true
        }

        val blackTheme: ATESwitchPreference? = findPreference(BLACK_THEME)
        blackTheme?.setOnPreferenceChangeListener { _, _ ->
            ThemeStore.markChanged(requireContext())
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            requireActivity().setTheme(PreferenceUtil.themeResFromPrefValue("black"))
            DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            restartActivity()
            true
        }


        desaturatedColor?.setOnPreferenceChangeListener { _, value ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            val desaturated = value as Boolean
            ThemeStore.prefs(requireContext()).edit {
                putBoolean(DESATURATED_COLOR, desaturated)
            }
            PreferenceUtil.isDesaturatedColor = desaturated
            restartActivity()
            true
        }

        val extraControls: TwoStatePreference? = findPreference(TOGGLE_ADD_CONTROLS)
        extraControls?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val languagePreference: ATEListPreference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            return@setOnPreferenceChangeListener true
        }

        val fontSize: Preference? = findPreference(FONT_SIZE)
        fontSize?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue as String
            ThemeStore.editTheme(requireContext()).fontSize(value).commit()
            restartActivity()
            true
        }

        val swipeGesturesNonFoldable: TwoStatePreference? =
            findPreference(TOGGLE_MINI_SWIPE_NON_FOLDABLE)
        swipeGesturesNonFoldable?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val transparentMiniPlayer: TwoStatePreference? = findPreference(TRANSPARENT_MINI_PLAYER)
        transparentMiniPlayer?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
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
        if (ApexUtil.isFoldable(requireContext())) {
            addPreferencesFromResource(R.xml.pref_ui_foldable)
        } else {
            addPreferencesFromResource(R.xml.pref_ui)
        }

        val accentColorPref: ATEColorPreference? = findPreference(ACCENT_COLOR)
        val desaturatedColor: ATESwitchPreference? = findPreference(DESATURATED_COLOR)
        val colorCategory: Preference? = findPreference("category_color")
        if (PreferenceUtil.getGeneralThemeValue() != ThemeMode.MD3) {
            ThemeStore.prefs(requireContext()).edit {
                putBoolean(DESATURATED_COLOR, PreferenceUtil.isDesaturatedColor)
            }
            accentColorPref?.isEnabled = true
            desaturatedColor?.isEnabled = true
            colorCategory?.isVisible = true
        }else {
            accentColorPref?.isEnabled = false
            desaturatedColor?.isEnabled = false
            colorCategory?.isVisible = false
        }

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