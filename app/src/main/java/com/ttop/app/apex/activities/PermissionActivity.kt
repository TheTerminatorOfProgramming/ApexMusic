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
package com.ttop.app.apex.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.VersionUtils
import com.ttop.app.apex.activities.base.AbsMusicServiceActivity
import com.ttop.app.apex.databinding.ActivityPermissionBinding
import com.ttop.app.apex.extensions.accentBackgroundColor
import com.ttop.app.apex.extensions.setStatusBarColorAuto
import com.ttop.app.apex.extensions.setTaskDescriptionColorAuto
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.util.RingtoneManager
import com.ttop.app.apex.views.PermissionItem

class PermissionActivity : AbsMusicServiceActivity() {
    private lateinit var binding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColorAuto()
        setTaskDescriptionColorAuto()
        setupTitle()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                binding.storagePermissions.isVisible = true
                binding.storageBTPermissions.isVisible =  false
            }else{
                binding.storagePermissions.isVisible = false
                binding.storageBTPermissions.isVisible =  true
            }

            binding.storageBTPermissions.setButtonClick {
                requestPermissions()
            }

            binding.storagePermissions.setButtonClick {
                requestPermissions()
            }
        }



        if (VersionUtils.hasMarshmallow()) {
            binding.audioPermission.show()
            binding.audioPermission.setButtonClick {
                if (RingtoneManager.requiresDialog(this@PermissionActivity)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = ("package:" + applicationContext.packageName).toUri()
                    startActivity(intent)
                }
            }
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
        val color = ThemeStore.accentColor(this)
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName = "Hello there! <br>Welcome to <b>Apex <span  style='color:$hexColor';>Music</span></b>"
            .parseAsHtml()
        binding.appNameText.text = appName
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                if (hasStoragePermission()) {
                    binding.storageBTPermissions.checkImage.isVisible = true
                    binding.storageBTPermissions.checkImage.imageTintList =
                        ColorStateList.valueOf(ThemeStore.accentColor(this))
                }
            }
            else{
                if (hasStoragePermission() && hasBtPermission()) {
                    binding.storageBTPermissions.checkImage.isVisible = true
                    binding.storageBTPermissions.checkImage.imageTintList =
                        ColorStateList.valueOf(ThemeStore.accentColor(this))
                }
            }
        }

        if (hasAudioPermission()) {
            binding.audioPermission.checkImage.isVisible = true
            binding.audioPermission.checkImage.imageTintList =
                ColorStateList.valueOf(ThemeStore.accentColor(this))
        }

        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasStoragePermission(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasAudioPermission(): Boolean {
        return Settings.System.canWrite(this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBtPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
