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

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.ttop.app.apex.R
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.databinding.FragmentSettingsBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.findNavController
import com.ttop.app.apex.model.CategoryInfo
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.activities.base.AbsSlidingMusicPanelActivity
import com.ttop.app.apex.ui.fragments.other.MiniPlayerFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.VersionUtils


class SettingsFragment : Fragment(R.layout.fragment_settings), ColorCallback {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSettingsBinding.bind(view)
        setupToolbar()
        if (ApexUtil.isTablet) {
            ApexUtil.setMargins(binding.contentFrame,0,0,0,ApexUtil.DpToMargin(0))
        }else {
            if (PreferenceUtil.libraryCategory.contains(CategoryInfo(CategoryInfo.Category.Settings, true))) {
                ApexUtil.setMargins(binding.contentFrame,0,0,0,ApexUtil.DpToMargin(80))
            }else {
                ApexUtil.setMargins(binding.contentFrame,0,0,0,ApexUtil.DpToMargin(0))
            }
        }
    }

    private fun setupToolbar() {
        val navController: NavController = findNavController(R.id.contentFrame)
        with (binding.appBarLayout.toolbar) {
            setNavigationIcon(R.drawable.ic_arrow_back)
            isTitleCentered = false
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.appBarLayout.title =
                navController.currentDestination?.let { getStringFromDestination(it) }.toString()
        }
    }

    private fun getStringFromDestination(currentDestination: NavDestination): String {
        val idRes = when (currentDestination.id) {
            R.id.mainSettingsFragment -> R.string.action_settings
            R.id.audioSettings -> R.string.pref_header_audio
            R.id.imageSettingFragment -> R.string.pref_header_images
            R.id.notificationSettingsFragment -> R.string.notification
            R.id.nowPlayingSettingsFragment -> R.string.now_playing
            R.id.otherSettingsFragment -> R.string.others
            R.id.personalizeSettingsFragment -> R.string.personalize
            R.id.themeSettingsFragment -> R.string.general_settings_title
            R.id.aboutActivity -> R.string.action_about
            R.id.backup_fragment -> R.string.backup_restore_title
            R.id.labs_fragment -> R.string.labs_title
            else -> R.id.action_settings
        }
        return getString(idRes)
    }

    override fun invoke(dialog: MaterialDialog, color: Int) {
        ThemeStore.editTheme(requireContext()).accentColor(color).commit()
        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
        activity?.recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
    }
}
