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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentMainSettingsBinding
import com.ttop.app.apex.extensions.drawAboveSystemBarsWithPadding
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil


class MainSettingsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentMainSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onClick(view: View) {
        findNavController().navigate(
            when (view.id) {
                R.id.generalSettings -> R.id.action_mainSettingsFragment_to_themeSettingsFragment
                R.id.audioSettings -> R.id.action_mainSettingsFragment_to_audioSettings
                R.id.imageSettings -> R.id.action_mainSettingsFragment_to_imageSettingFragment
                R.id.personalizeSettings -> R.id.action_mainSettingsFragment_to_personalizeSettingsFragment
                R.id.notificationSettings -> R.id.action_mainSettingsFragment_to_notificationSettingsFragment
                R.id.otherSettings -> R.id.action_mainSettingsFragment_to_otherSettingsFragment
                R.id.aboutSettings -> R.id.action_mainSettingsFragment_to_aboutActivity
                R.id.nowPlayingSettings -> R.id.action_mainSettingsFragment_to_nowPlayingSettingsFragment
                R.id.backup_restore_settings -> R.id.action_mainSettingsFragment_to_backupFragment
                R.id.labsSettings -> R.id.action_mainSettingsFragment_to_labsFragment
                else -> R.id.action_mainSettingsFragment_to_themeSettingsFragment
            }
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.generalSettings.setOnClickListener(this)
        binding.audioSettings.setOnClickListener(this)
        binding.nowPlayingSettings.setOnClickListener(this)
        binding.personalizeSettings.setOnClickListener(this)
        binding.notificationSettings.setOnClickListener(this)
        binding.otherSettings.setOnClickListener(this)
        binding.imageSettings.setOnClickListener(this)
        binding.backupRestoreSettings.setOnClickListener(this)

        binding.aboutSettings.setOnClickListener(this)

        binding.labsSettings.setOnClickListener(this)

        binding.container.drawAboveSystemBarsWithPadding()

        if (PreferenceUtil.isDevModeEnabled) {
            binding.labsSettings.visibility = View.VISIBLE
        }else {
            binding.labsSettings.visibility = View.GONE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}