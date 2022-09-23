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
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
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
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.NavigationUtil
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

    private fun setUpView() {


        binding.aboutContent.cardApexInfo.appGithub.setOnClickListener(this)
        binding.aboutContent.cardApexInfo.appShare.setOnClickListener(this)
        //binding.aboutContent.cardApexInfo.websiteLink.setOnClickListener(this)

        binding.aboutContent.cardOther.changelog.setOnClickListener(this)
        binding.aboutContent.cardOther.openSource.setOnClickListener(this)

        binding.aboutContent.cardPermissions?.storagePermission?.text = checkStoragePermission()
        binding.aboutContent.cardPermissions?.btPermission?.text = checkBtPermission()
        if (VersionUtils.hasS()) {
            binding.aboutContent.cardPermissions?.batteryPermission?.text = checkBatteryOptimization()

        }
        binding.aboutContent.cardPermissions?.permissionsEdit?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.appGithub -> openUrl(Constants.GITHUB_PROJECT)
            R.id.appShare -> shareApp()
            R.id.changelog -> NavigationUtil.gotoWhatNews(requireActivity())
            R.id.openSource -> NavigationUtil.goToOpenSource(requireActivity())
            R.id.permissions_edit -> goToPermissions()
            R.id.battery_permission_title -> ApexUtil.disableBatteryOptimization()
        }
    }

    private fun getAppVersion(): String {
        if (BuildConfig.BUILD_TYPE.equals("beta") || BuildConfig.DEBUG) {
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val formattedDate = df.format(c)
            val tf = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
            val formattedTime = tf.format(c)
            try {
                return requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).versionName + "_" + formattedDate + "_" + formattedTime
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        } else {
            try {
                return requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        return "Unknown"
    }

    private fun getRetroMusicVersion(): String {
        return "6.0.2 Beta"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
