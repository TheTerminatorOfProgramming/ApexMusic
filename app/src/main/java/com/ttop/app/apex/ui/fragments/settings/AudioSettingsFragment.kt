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

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.AUTO_DOWNLOAD_IMAGES_POLICY
import com.ttop.app.apex.BLUETOOTH_DELAY
import com.ttop.app.apex.BLUETOOTH_DEVICE
import com.ttop.app.apex.BLUETOOTH_PLAYBACK
import com.ttop.app.apex.EQUALIZER
import com.ttop.app.apex.GAP_LESS_PLAYBACK
import com.ttop.app.apex.PAUSE_ON_ZERO_VOLUME
import com.ttop.app.apex.R
import com.ttop.app.apex.SPECIFIC_DEVICE
import com.ttop.app.apex.TOGGLE_HEADSET
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.apex.ui.activities.base.AbsBaseActivity.Companion.BLUETOOTH_PERMISSION_REQUEST
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil


/**
 * @author Hemanth S (h4h13).
 */

class AudioSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun invalidateSettings() {
        val eqPreference: Preference? = findPreference(EQUALIZER)
        eqPreference?.setOnPreferenceClickListener {
            NavigationUtil.openEqualizer(requireActivity())
            true
        }

        val pause: TwoStatePreference? = findPreference(PAUSE_ON_ZERO_VOLUME)
        pause?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val gapless: TwoStatePreference? = findPreference(GAP_LESS_PLAYBACK)
        gapless?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val autoplay: TwoStatePreference? = findPreference(TOGGLE_HEADSET)
        autoplay?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val bluetoothPreference: Preference? = findPreference(BLUETOOTH_PLAYBACK)
        bluetoothPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            val value = newValue as Boolean

            if (value) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        BLUETOOTH_CONNECT
                    ) != PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(
                            BLUETOOTH_CONNECT
                        ), BLUETOOTH_PERMISSION_REQUEST
                    )
                }
            }
            return@setOnPreferenceChangeListener true
        }

        val specificDevice: TwoStatePreference? = findPreference(SPECIFIC_DEVICE)
        specificDevice?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_audio)

        val bluetoothDelay: Preference? = findPreference(BLUETOOTH_DELAY)

        var value = (PreferenceUtil.bluetoothDelay / 1000).toString()

        value = if (value > "1") {
            "$value sec"
        } else {
            "$value secs"
        }

        if (PreferenceUtil.isBluetoothSpeaker) {
            bluetoothDelay?.setSummary(value)
        } else {
            bluetoothDelay?.setSummary("")
        }

        val bluetoothDevice: Preference? = findPreference(BLUETOOTH_DEVICE)
        val device = (PreferenceUtil.bluetoothDevice)
        if (PreferenceUtil.specificDevice) {
            bluetoothDevice?.setSummary(device)
        } else {
            bluetoothDevice?.setSummary("")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
        val preference: Preference? = findPreference(AUTO_DOWNLOAD_IMAGES_POLICY)
        preference?.let { setSummary(it) }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BLUETOOTH_PLAYBACK -> {
                val bluetoothDelay: Preference? = findPreference(BLUETOOTH_DELAY)

                var value = (PreferenceUtil.bluetoothDelay / 1000).toString()

                value = if (value > "1") {
                    "$value sec"
                } else {
                    "$value secs"
                }

                if (PreferenceUtil.isBluetoothSpeaker) {
                    bluetoothDelay?.setSummary(value)
                } else {
                    bluetoothDelay?.setSummary("")
                }
            }
            BLUETOOTH_DELAY -> {
                val bluetoothDelay: Preference? = findPreference(BLUETOOTH_DELAY)

                var value = (PreferenceUtil.bluetoothDelay / 1000).toString()

                value = if (value > "1") {
                    "$value sec"
                } else {
                    "$value secs"
                }

                bluetoothDelay?.setSummary(value)
            }
            SPECIFIC_DEVICE -> {
                val bluetoothDevice: Preference? = findPreference(BLUETOOTH_DEVICE)
                val device = (PreferenceUtil.bluetoothDevice)
                if (PreferenceUtil.specificDevice) {
                    bluetoothDevice?.setSummary(device)
                } else {
                    bluetoothDevice?.setSummary("")
                }
            }
            BLUETOOTH_DEVICE -> {
                val bluetoothDevice: Preference? = findPreference(BLUETOOTH_DEVICE)
                val device = (PreferenceUtil.bluetoothDevice.toString())
                bluetoothDevice?.setSummary(device)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }
}
