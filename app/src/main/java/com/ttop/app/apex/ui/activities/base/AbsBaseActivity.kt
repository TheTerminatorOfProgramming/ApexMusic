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
package com.ttop.app.apex.ui.activities.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.rootView
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.util.logD

abstract class AbsBaseActivity : AbsThemeActivity() {
    private var hadPermissions: Boolean = false
    private lateinit var permissions: Array<String>
    private var permissionDeniedMessage: String? = null

    open fun getPermissionsToRequest(): Array<String> {
        return arrayOf()
    }

    protected fun setPermissionDeniedMessage(message: String) {
        permissionDeniedMessage = message
    }

    fun getPermissionDeniedMessage(): String {
        return if (permissionDeniedMessage == null) getString(R.string.permissions_denied) else permissionDeniedMessage!!
    }

    private val snackBarContainer: View
        get() = rootView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        permissions = getPermissionsToRequest()
        hadPermissions = hasPermissions()
        permissionDeniedMessage = null
    }

    override fun onResume() {
        super.onResume()
        val hasPermissions = hasPermissions()
        if (hasPermissions != hadPermissions) {
            hadPermissions = hasPermissions
            onHasPermissionsChanged(hasPermissions)
        }
    }

    protected open fun onHasPermissionsChanged(hasPermissions: Boolean) {
        // implemented by sub classes
        logD(hasPermissions)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_MENU && event.action == KeyEvent.ACTION_UP) {
            showOverflowMenu()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showOverflowMenu() {
    }

    protected open fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
    }

    protected fun hasPermissions(): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    if (VersionUtils.hasT()) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this@AbsBaseActivity, Manifest.permission.READ_MEDIA_AUDIO,
                            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                                this@AbsBaseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            )
                        ) {
                            // User has deny from permission dialog
                            Snackbar.make(
                                snackBarContainer,
                                permissionDeniedMessage!!,
                                Snackbar.LENGTH_SHORT
                            )
                                .setAction(R.string.action_grant) {
                                    requestPermissions()
                                }
                                .setActionTextColor(accentColor()).show()
                        } else {
                            // User has deny permission and checked never show permission dialog so you can redirect to Application settings page
                            Snackbar.make(
                                snackBarContainer,
                                permissionDeniedMessage!!,
                                Snackbar.LENGTH_INDEFINITE
                            )
                                .setAction(R.string.action_settings) {
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    val uri = Uri.fromParts(
                                        "package",
                                        this@AbsBaseActivity.packageName,
                                        null
                                    )
                                    intent.data = uri
                                    startActivity(intent)
                                }.setActionTextColor(accentColor()).show()
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this@AbsBaseActivity, Manifest.permission.READ_EXTERNAL_STORAGE,
                            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                                this@AbsBaseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            )
                        ) {
                            // User has deny from permission dialog
                            Snackbar.make(
                                snackBarContainer,
                                permissionDeniedMessage!!,
                                Snackbar.LENGTH_SHORT
                            )
                                .setAction(R.string.action_grant) { requestPermissions() }
                                .setActionTextColor(accentColor()).show()
                        } else {
                            // User has deny permission and checked never show permission dialog so you can redirect to Application settings page
                            Snackbar.make(
                                snackBarContainer,
                                permissionDeniedMessage!!,
                                Snackbar.LENGTH_INDEFINITE
                            )
                                .setAction(R.string.action_settings) {
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    val uri = Uri.fromParts(
                                        "package",
                                        this@AbsBaseActivity.packageName,
                                        null
                                    )
                                    intent.data = uri
                                    startActivity(intent)
                                }.setActionTextColor(accentColor()).show()
                        }
                    }
                    return
                }
            }
            hadPermissions = true
            onHasPermissionsChanged(true)
        }
    }

    companion object {
        const val PERMISSION_REQUEST = 100
        const val BLUETOOTH_PERMISSION_REQUEST = 101
    }

    // this lets keyboard close when clicked in background
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(
                        v.windowToken,
                        0
                    )
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
