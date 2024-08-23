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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorCallback
import com.ttop.app.apex.R
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.databinding.FragmentSettingsBinding
import com.ttop.app.apex.extensions.findNavController
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.ui.fragments.base.AbsMainActivityFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.appthemehelper.ThemeStore

class SettingsFragment : AbsMainActivityFragment(R.layout.fragment_settings), ColorCallback{
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)
        setupToolbar()
        binding.contentFrame.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                ApexUtil.dpToMargin(64)
            } else {
                ApexUtil.dpToMargin(0)
            }
        }
    }

    private fun setupToolbar() {
        val navController: NavController = findNavController(R.id.contentFrame)
        with(binding.appBarLayout.toolbar) {
            setNavigationIcon(R.drawable.ic_arrow_back)

            isTitleCentered = true
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.appBarLayout.title =
                navController.currentDestination?.let { getStringFromDestination(it) }.toString()

            binding.appBarLayout.toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
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
            else -> R.id.action_settings
        }
        return getString(idRes)
    }

    override fun invoke(dialog: MaterialDialog, color: Int) {
        ThemeStore.editTheme(requireContext()).accentColor(color).commit()
        DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
        activity?.recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        binding.contentFrame.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (ApexUtil.isTablet) {
                ApexUtil.dpToMargin(0)
            } else {
                if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                    ApexUtil.dpToMargin(64)
                } else {
                    ApexUtil.dpToMargin(0)
                }
            }
        }
    }

    override fun onServiceConnected() {}

    override fun onServiceDisconnected() {}

    override fun onFavoriteStateChanged() {}

    override fun onPlayingMetaChanged() {}

    override fun onPlayStateChanged() {}

    override fun onRepeatModeChanged() {}

    override fun onShuffleModeChanged() {}

    override fun onMediaStoreChanged() {}

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}
