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
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.View
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.Constants
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.ContributorAdapter
import com.ttop.app.apex.databinding.FragmentAboutBinding
import com.ttop.app.apex.extensions.openUrl
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.ui.activities.AppIntroActivityAbout
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.NavigationUtil
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
    }

    override fun onResume() {
        super.onResume()

        binding.aboutContent.cardPermissions.storagePermission.text = checkStoragePermission()
        binding.aboutContent.cardPermissions.btPermission.text = checkBtPermission()
        binding.aboutContent.cardPermissions.batteryPermission.text = checkBatteryOptimization()
    }

    private fun setUpView() {
        binding.aboutContent.cardApexInfo.appGithub.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.appShare.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.websiteLink.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.appRate.setOnClickListener(this)
        binding.aboutContent.cardOther.changelog.setOnClickListener(this)
        binding.aboutContent.cardOther.openSource.setOnClickListener(this)
        binding.aboutContent.cardPermissions.storagePermission.text = checkStoragePermission()
        binding.aboutContent.cardPermissions.btPermission.text = checkBtPermission()
        binding.aboutContent.cardPermissions.batteryPermission.text = checkBatteryOptimization()
        binding.aboutContent.cardPermissions.permissionsEdit.setOnClickListener(this)
        binding.aboutContent.cardPermissions.ringtonePermission.setOnClickListener(this)
        binding.aboutContent.cardPermissions.intro.setOnClickListener(this)
        binding.aboutContent.cardTroubleshoot.forceClose.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.telegramLink.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.crowdinLink.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.appGithub -> openUrl(Constants.GITHUB_PROJECT)
            R.id.appRate -> openUrl(Constants.RATE_ON_GOOGLE_PLAY)
            R.id.appShare -> shareApp()
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
        if (BuildConfig.BUILD_TYPE.equals("beta") || BuildConfig.DEBUG) {
            try {
                return requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).versionName + " " + getString(R.string.play_store_edition_debug) + " " + formattedDate + "_" + formattedTime
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        } else {
            if (BuildConfig.BUILD_TYPE.equals("preview")) {
                try {
                    return requireContext().packageManager.getPackageInfo(
                        requireContext().packageName,
                        0
                    ).versionName + " " + getString(R.string.play_store_edition_preview)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    return requireContext().packageManager.getPackageInfo(
                        requireContext().packageName,
                        0
                    ).versionName + " " + getString(R.string.play_store_edition)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

        return "Unknown"
    }

    private fun getRetroMusicVersion(): String {
        return "6.2.0 Production"
    }

    private fun shareApp() {
        ShareCompat.IntentBuilder(requireActivity()).setType("text/plain")
            .setChooserTitle(R.string.share_app)
            .setText(String.format(getString(R.string.app_share), requireActivity().packageName))
            .startChooser()
    }

    private fun loadContributors() {
        val contributorAdapter = ContributorAdapter(emptyList())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
