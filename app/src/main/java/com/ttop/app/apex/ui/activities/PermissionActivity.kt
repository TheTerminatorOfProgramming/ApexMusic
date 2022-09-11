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
package com.ttop.app.apex.ui.activities

import android.Manifest
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.ActivityPermissionBinding
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.ui.activities.base.AbsMusicServiceActivity
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.appthemehelper.util.VersionUtils

class PermissionActivity : AbsMusicServiceActivity() {
    private lateinit var binding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColorAuto()
        setTaskDescriptionColorAuto()
        setupTitle()

        binding.storagePermission.setButtonClick {
            requestPermissions()
        }
        binding.audioPermission.show()
        binding.audioPermission.setButtonClick {
            if (!hasAudioPermission()) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = ("package:" + applicationContext.packageName).toUri()
                startActivity(intent)
            }
        }


        if (VersionUtils.hasS()) {
            binding.bluetoothPermission.show()
            binding.bluetoothPermission.setButtonClick {
                ActivityCompat.requestPermissions(this,
                    arrayOf(BLUETOOTH_CONNECT),
                    BLUETOOTH_PERMISSION_REQUEST)
            }

            binding.batteryPermission.show()
            binding.batteryPermission.setButtonClick {
                val intent = Intent()
                val packageName = packageName
                val pm = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        } else {
            binding.audioPermission.setNumber("2")
        }

        binding.finish.accentBackgroundColor()
        binding.finish.setOnClickListener {
            if (hasPermissions()) {
                startActivity(
                    Intent(this, MainActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
                finish()
            }
        }
    }

    private fun setupTitle() {
        val color = accentColor()
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName =
            getString(R.string.message_welcome,
                "<b>Apex <span  style='color:$hexColor';>Music</span></b>")
                .parseAsHtml()
        binding.appNameText.text = appName
    }

    override fun onResume() {
        super.onResume()

        binding.finish.isEnabled = if (VersionUtils.hasS()) {
            hasStoragePermission() && ApexUtil.hasBatteryPermission() && hasBluetoothPermission()
        }else{
            hasStoragePermission()
        }

        if (hasStoragePermission()) {
            binding.storagePermission.checkImage.isVisible = true
            binding.storagePermission.checkImage.imageTintList =
                ColorStateList.valueOf(accentColor())
        }
        if (hasAudioPermission()) {
            binding.audioPermission.checkImage.isVisible = true
            binding.audioPermission.checkImage.imageTintList =
                ColorStateList.valueOf(accentColor())
        }
        if (VersionUtils.hasS()) {
            if (hasBluetoothPermission()) {
                binding.bluetoothPermission.checkImage.isVisible = true
                binding.bluetoothPermission.checkImage.imageTintList =
                    ColorStateList.valueOf(accentColor())
            }
        }

        if (VersionUtils.hasS()){
            if (ApexUtil.hasBatteryPermission()) {
                binding.batteryPermission.checkImage.isVisible = true
                binding.batteryPermission.checkImage.imageTintList =
                    ColorStateList.valueOf(accentColor())
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (VersionUtils.hasT()){
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        }else{
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBluetoothPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
            BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasAudioPermission(): Boolean {
        return Settings.System.canWrite(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}