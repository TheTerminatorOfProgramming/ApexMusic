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

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.input.input
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentMainSettingsBinding
import com.ttop.app.apex.extensions.applyToolbar
import com.ttop.app.apex.extensions.drawAboveSystemBarsWithPadding
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.helper.BackupHelper
import com.ttop.app.apex.helper.sanitize
import com.ttop.app.apex.ui.fragments.backup.BackupFragment
import com.ttop.app.apex.ui.fragments.backup.RestoreActivity
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.views.SettingListItemView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

    fun showCreateBackupDialog() {
        materialDialog().show {
            title(res = R.string.action_rename)
            input(prefill = BackupHelper.getTimeStamp()) { _, text ->
                // Text submitted with the action button
                lifecycleScope.launch {
                    BackupHelper.createBackup(requireContext(), text.sanitize())
                }
            }
            positiveButton(android.R.string.ok)
            negativeButton(R.string.action_cancel)
            setTitle(R.string.title_new_backup)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun AlertDialog.withCenteredButtons() {
        val positive = getButton(AlertDialog.BUTTON_POSITIVE)
        val negative = getButton(AlertDialog.BUTTON_NEGATIVE)

        //Disable the material spacer view in case there is one
        val parent = positive.parent as? LinearLayout
        parent?.gravity = Gravity.CENTER_HORIZONTAL
        val leftSpacer = parent?.getChildAt(1)
        leftSpacer?.visibility = View.GONE

        //Force the default buttons to center
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.weight = 1f
        layoutParams.gravity = Gravity.CENTER

        positive.layoutParams = layoutParams
        negative.layoutParams = layoutParams
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
        /*
        binding.backupRestoreSettings.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            builder.setTitle("Backup/Restore")
            builder.setMessage("Would you like to create or restore a backup?")

            builder.setPositiveButton("Restore") { _, _ -> // Do nothing but close the dialog
                openFilePicker.launch(arrayOf("application/octet-stream"))
            }

            builder.setNegativeButton(
                "Create"
            ) { _, _ -> // Do nothing
                showCreateBackupDialog()
            }

            val alert = builder.create()
            alert.show()
            alert.withCenteredButtons()
        }*/
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
