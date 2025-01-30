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
package com.ttop.app.apex.ui.fragments.about

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.Constants
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.ContributorAdapter
import com.ttop.app.apex.databinding.FragmentAboutBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.openUrl
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.ui.activities.AppIntroActivityAbout
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode
import dev.chrisbanes.insetter.applyInsetter
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AboutFragment : Fragment(R.layout.fragment_about), View.OnClickListener {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)

        binding.aboutContent.cardOther.version.setSummary(getAppVersion())
        binding.aboutContent.cardOther.retroVersion.setSummary(getRetroMusicVersion())


        setUpView()
        loadContributors()

        binding.aboutContent.root.applyInsetter {
            type(navigationBars = true) {
                padding(vertical = true)
            }
        }
        val final = String.format(getString(R.string.all_files_summary), getString(R.string.name))
        binding.aboutContent.cardPermissions.allFilesPermission.setSummary(final)

        val final1 = String.format(getString(R.string.force_close_summary), getString(R.string.name))
        binding.aboutContent.cardTroubleshoot.forceClose.setSummary(final1)

        binding.aboutContent.cardPermissions.aboutCard.strokeColor = accentColor()
        binding.aboutContent.cardOther.aboutCard.strokeColor = accentColor()
        binding.aboutContent.cardTroubleshoot.aboutCard.strokeColor = accentColor()
        binding.aboutContent.cardCredit.aboutCard.strokeColor = accentColor()
        binding.aboutContent.cardApexInfo.aboutCard.strokeColor = accentColor()

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                    }
                    else -> {
                        binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.AUTO_BLACK -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                    }
                    else -> {
                        binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
            }
            ThemeMode.LIGHT -> {
                binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
            }
            ThemeMode.MD3 -> {
                binding.aboutContent.cardPermissions.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardOther.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardTroubleshoot.sb4.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardCredit.sb1.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardApexInfo.sb2.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
            }
        }


        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))

                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                    }
                    else -> {
                        binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.AUTO_BLACK -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                    }
                    else -> {
                        binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
            }
            ThemeMode.LIGHT -> {
                binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
            }
            ThemeMode.MD3 -> {
                binding.aboutContent.cardPermissions.storagePermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardPermissions.btPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardPermissions.batteryPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardPermissions.filesPermissionTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardPermissions.storagePermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardPermissions.btPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardPermissions.batteryPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
                binding.aboutContent.cardPermissions.filesPermission.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
            }
        }
        binding.aboutContent.cardPermissions.sb4.setTextColor(accentColor())
        binding.aboutContent.cardOther.sb4.setTextColor(accentColor())
        binding.aboutContent.cardTroubleshoot.sb4.setTextColor(accentColor())
        binding.aboutContent.cardCredit.sb1.setTextColor(accentColor())
        binding.aboutContent.cardApexInfo.sb2.setTextColor(accentColor())
    }

    override fun onResume() {
        super.onResume()

        binding.aboutContent.cardPermissions.storagePermission.text = checkStoragePermission()
        binding.aboutContent.cardPermissions.btPermission.text = checkBtPermission()
        binding.aboutContent.cardPermissions.batteryPermission.text = checkBatteryOptimization()
        binding.aboutContent.cardPermissions.filesPermission.text = checkFilesPermission()
    }

    private fun setUpView() {
        binding.aboutContent.cardApexInfo.appGithub.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.websiteLink.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.telegramLink.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.discordLink.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.crowdinLink.setOnClickListener(this)
        binding.aboutContent.cardOther.changelog.setOnClickListener(this)
        binding.aboutContent.cardOther.openSource.setOnClickListener(this)
        binding.aboutContent.cardOther.checkVersion.setOnClickListener(this)
        binding.aboutContent.cardPermissions.storagePermission.text = checkStoragePermission()
        binding.aboutContent.cardPermissions.btPermission.text = checkBtPermission()
        binding.aboutContent.cardPermissions.batteryPermission.text = checkBatteryOptimization()
        binding.aboutContent.cardPermissions.filesPermission.text = checkFilesPermission()
        binding.aboutContent.cardPermissions.permissionsEdit.setOnClickListener(this)
        binding.aboutContent.cardPermissions.ringtonePermission.setOnClickListener(this)
        binding.aboutContent.cardPermissions.allFilesPermission.setOnClickListener(this)
        binding.aboutContent.cardPermissions.intro.setOnClickListener(this)
        binding.aboutContent.cardTroubleshoot.forceClose.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.appGithub -> openUrl(Constants.GITHUB_PROJECT)
            R.id.websiteLink -> goToWebPage()
            R.id.changelog -> NavigationUtil.gotoWhatNews(requireActivity())
            R.id.openSource -> NavigationUtil.goToOpenSource(requireActivity())
            R.id.permissions_edit -> goToPermissions()
            R.id.ringtone_permission -> {
                if (!ApexUtil.hasAudioPermission()) {
                    ApexUtil.enableAudioPermission(requireContext())
                } else {
                    showToast(R.string.permission_granted)
                }
            }

            R.id.all_files_permission -> context?.let { ApexUtil.manageAllFiles(it) }
            R.id.battery_permission_title -> ApexUtil.disableBatteryOptimization()
            R.id.intro -> startActivity(
                Intent(
                    activity,
                    AppIntroActivityAbout::class.java
                )
            )

            R.id.force_close -> {
                val id = Process.myPid()
                Process.killProcess(id)
            }

            R.id.telegramLink -> {
                goToTelegramGroup()
            }

            R.id.crowdinLink -> {
                goToCrowdin()
            }

            R.id.discordLink -> {
                goToDiscordGroup()
            }
            R.id.checkVersion -> ApexUtil.checkForUpdates(requireActivity(),
                update = true,
                autoCheck = false
            )
        }
    }

    private fun goToWebPage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://theterminatorofprogramming.github.io/")
        startActivity(intent)
    }

    private fun goToTelegramGroup() {
        try {
            val telegramIntent = Intent(Intent.ACTION_VIEW)
            telegramIntent.data = Uri.parse("https://t.me/ApexMusicSupport")
            startActivity(telegramIntent)
        } catch (e: Exception) {
            showToast(e.message.toString())
        }
    }

    private fun goToDiscordGroup() {
        try {
            val discordIntent = Intent(Intent.ACTION_VIEW)
            discordIntent.data = Uri.parse("https://discord.gg/fxNbXs5AgX")
            startActivity(discordIntent)
        } catch (e: Exception) {
            showToast(e.message.toString())
        }
    }

    private fun goToCrowdin() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://crowdin.com/project/apexmusic")
        startActivity(intent)
    }

    private fun getAppVersion(): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val formattedDate = df.format(c)
        val tf = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        val formattedTime = tf.format(c)
        if (BuildConfig.DEBUG) {
            //BETA
            try {
                return requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).versionName + " " + getString(R.string.beta) + " " + formattedDate + "_" + formattedTime
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        } else {
            try {
                return requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).versionName!!
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        return getString(R.string.unknown)
    }

    private fun getRetroMusicVersion(): String {
        return getString(R.string.retro_base)
    }

    private fun loadContributors() {
        val contributorAdapter = ContributorAdapter(emptyList(), requireContext())
        binding.aboutContent.cardCredit.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            adapter = contributorAdapter
        }
        libraryViewModel.fetchContributors().observe(viewLifecycleOwner) { contributors ->
            contributorAdapter.swapData(contributors)
        }
    }

    private fun goToPermissions() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun checkStoragePermission(): String {
        return if (VersionUtils.hasT()) {
            if (activity?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                }
                == PackageManager.PERMISSION_GRANTED) {
                getString(R.string.granted) + " ✅"
            } else {
                getString(R.string.denied) + " ❌"
            }
        } else {
            if (activity?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
                == PackageManager.PERMISSION_GRANTED) {
                getString(R.string.granted) + " ✅"
            } else {
                getString(R.string.denied) + " ❌"
            }
        }
    }

    private fun checkBtPermission(): String {
        return if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            getString(R.string.granted) + " ✅"
        } else {
            getString(R.string.denied) + " ❌"
        }
    }

    private fun checkBatteryOptimization(): String {
        val packageName = context?.packageName
        val pm = context?.getSystemService(ATHToolbarActivity.POWER_SERVICE) as PowerManager

        return if (pm.isIgnoringBatteryOptimizations(packageName)) {
            getString(R.string.disabled) + " ✅"
        } else {
            getString(R.string.enabled) + " ❌"
        }
    }

    private fun checkFilesPermission(): String {
        return if (Environment.isExternalStorageManager()) {
            getString(R.string.granted) + " ✅"
        } else {
            getString(R.string.denied) + " ❌"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
