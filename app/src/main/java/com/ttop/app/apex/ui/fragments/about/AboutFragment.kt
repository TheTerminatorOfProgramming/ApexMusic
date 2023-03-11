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
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.Constants
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.ContributorAdapter
import com.ttop.app.apex.databinding.FragmentAboutBinding
import com.ttop.app.apex.extensions.openUrl
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.appthemehelper.util.VersionUtils
import dev.chrisbanes.insetter.applyInsetter
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*


class AboutFragment : Fragment(R.layout.fragment_about), View.OnClickListener {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val libraryViewModel by sharedViewModel<LibraryViewModel>()
    private var count: Int = 0
    private val timer = object: CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (count==10){
                PreferenceUtil.isDevModeEnabled = true
                showToast("Developer Mode On!")
            }

            count =0

            if (!PreferenceUtil.isDevModeEnabled) {
                binding.aboutContent.cardOther.devMode.visibility = View.GONE
                binding.aboutContent.cardOther.devMode.setSummary(getText(R.string.off) as String)
            } else {
                binding.aboutContent.cardOther.devMode.visibility = View.VISIBLE
                binding.aboutContent.cardOther.devMode.setSummary(getText(R.string.on) as String)
            }
        }
    }
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

        if (!PreferenceUtil.isDevModeEnabled) {
            binding.aboutContent.cardOther.devMode.visibility = View.GONE
            binding.aboutContent.cardOther.devMode.setSummary(getText(R.string.off) as String)
        } else {
            binding.aboutContent.cardOther.devMode.visibility = View.VISIBLE
            binding.aboutContent.cardOther.devMode.setSummary(getText(R.string.on) as String)
        }
        PreferenceUtil.isAppInstalledFromGooglePlay = getInstallerPackageName().contains("com.android.vending")

        if (PreferenceUtil.isAppInstalledFromGooglePlay){
            binding.aboutContent.cardOther.update.visibility = View.GONE
        }else {
            binding.aboutContent.cardOther.update.visibility = View.VISIBLE
        }
    }

    private fun setUpView() {


        binding.aboutContent.cardApexInfo.appGithub.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.appShare.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.websiteLink.setOnClickListener(this)

        binding.aboutContent.cardOther.changelog.setOnClickListener(this)
        binding.aboutContent.cardOther.openSource.setOnClickListener(this)

        binding.aboutContent.cardPermissions?.storagePermission?.text = checkStoragePermission()
        binding.aboutContent.cardPermissions?.btPermission?.text = checkBtPermission()
        if (VersionUtils.hasS()) {
            binding.aboutContent.cardPermissions?.batteryPermission?.text = checkBatteryOptimization()

        }
        binding.aboutContent.cardPermissions?.permissionsEdit?.setOnClickListener(this)
        binding.aboutContent.cardOther.version.setOnClickListener(this)
        binding.aboutContent.cardOther.devMode.setOnClickListener(this)
        binding.aboutContent.cardOther.update.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.appGithub -> openUrl(Constants.GITHUB_PROJECT)
            R.id.appShare -> shareApp()
            R.id.websiteLink -> goToWebPage()
            R.id.changelog -> NavigationUtil.gotoWhatNews(requireActivity())
            R.id.openSource -> NavigationUtil.goToOpenSource(requireActivity())
            R.id.permissions_edit -> goToPermissions()
            R.id.battery_permission_title -> ApexUtil.disableBatteryOptimization()
            R.id.version -> {
                if (!PreferenceUtil.isDevModeEnabled) {
                    if (count == 0) {
                        count +=1
                        timer.start()
                    }else {
                        if (count < 10) {
                            count +=1
                        }else {
                            timer.cancel()
                            showToast("Developer Mode On!")
                            PreferenceUtil.isDevModeEnabled = true
                            count = 0

                        }
                    }
                }else {
                    showToast("Developer mode already turned on!")
                }

                if (!PreferenceUtil.isDevModeEnabled) {
                    binding.aboutContent.cardOther.devMode.visibility = View.GONE
                    binding.aboutContent.cardOther.devMode.setSummary(getText(R.string.off) as String)
                } else {
                    binding.aboutContent.cardOther.devMode.visibility = View.VISIBLE
                    binding.aboutContent.cardOther.devMode.setSummary(getText(R.string.on) as String)
                }
            }
            R.id.devMode -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Developer Mode")
                builder.setMessage("Turn off Developer Mode?")

                builder.setPositiveButton(android.R.string.yes) { _, _ ->
                    PreferenceUtil.isDevModeEnabled = false

                    binding.aboutContent.cardOther.devMode.setSummary(getText(R.string.off) as String)

                    binding.aboutContent.cardOther.devMode.visibility = View.GONE
                }

                builder.setNegativeButton(android.R.string.no) { _, _ ->
                }
                builder.show()
            }
            R.id.update -> {
                val appUpdater = AppUpdater(activity)
                    .setUpdateFrom(UpdateFrom.GITHUB)
                    .setGitHubUserAndRepo("TheTerminatorOfProgramming", "ApexMusic")
                    .setDisplay(Display.DIALOG)
                    .showAppUpdated(true)
                appUpdater.start()
            }
        }
    }

    fun goToWebPage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://theterminatorofprogramming.github.io/")
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
                ).versionName + " Build " + formattedDate + "_" + formattedTime
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        } else {
            try {
                return requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        return "Unknown"
    }

    private fun getRetroMusicVersion(): String {
        return "6.0.4 BETA"
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
            if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_MEDIA_AUDIO) }
                == PackageManager.PERMISSION_GRANTED) {
                "Granted"
            }else{
                "Denied"
            }
        } else {
            if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
                == PackageManager.PERMISSION_GRANTED) {
                "Granted"
            }else{
                "Denied"
            }
        }
    }

    private fun checkBtPermission(): String {
        return if (VersionUtils.hasS()) {
            if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.BLUETOOTH_CONNECT) }
                == PackageManager.PERMISSION_GRANTED) {
                "Granted"
            }else{
                "Denied"
            }
        } else {
            if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.BLUETOOTH) }
                == PackageManager.PERMISSION_GRANTED) {
                "Granted"
            }else{
                "Denied"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBatteryOptimization(): String {
        val packageName = context?.packageName
        val pm = context?.getSystemService(ATHToolbarActivity.POWER_SERVICE) as PowerManager

        return if (pm.isIgnoringBatteryOptimizations(packageName)) {
            "Disabled"
        } else {
            "Enabled"
        }
    }

    private fun getInstallerPackageName(): String {
        return if (VersionUtils.hasR()){
            requireContext().packageManager.getInstallSourceInfo(requireContext().packageName).installingPackageName.toString()
        }else {
            requireContext().packageManager.getInstallerPackageName(requireContext().packageName).toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
