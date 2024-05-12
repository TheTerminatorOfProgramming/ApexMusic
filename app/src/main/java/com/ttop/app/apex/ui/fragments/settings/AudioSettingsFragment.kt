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
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.AUTO_DOWNLOAD_IMAGES_POLICY
import com.ttop.app.apex.BLUETOOTH_DEVICE
import com.ttop.app.apex.BLUETOOTH_PLAYBACK
import com.ttop.app.apex.EQUALIZER
import com.ttop.app.apex.GAP_LESS_PLAYBACK
import com.ttop.app.apex.MANAGE_AUDIO_FOCUS
import com.ttop.app.apex.PAUSE_ON_ZERO_VOLUME
import com.ttop.app.apex.R
import com.ttop.app.apex.SPECIFIC_DEVICE
import com.ttop.app.apex.TOGGLE_HEADSET
import com.ttop.app.apex.ui.activities.base.AbsBaseActivity.Companion.BLUETOOTH_PERMISSION_REQUEST
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference


/**
 * @author Hemanth S (h4h13).
 */

class AudioSettingsFragment : AbsSettingsFragment() {

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

        val manageAudio: TwoStatePreference? = findPreference(MANAGE_AUDIO_FOCUS)
        manageAudio?.setOnPreferenceChangeListener { _, _ ->
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
                if (ActivityCompat.checkSelfPermission(requireContext(),
                        BLUETOOTH_CONNECT) != PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                        BLUETOOTH_CONNECT), BLUETOOTH_PERMISSION_REQUEST)
                }
            }
            return@setOnPreferenceChangeListener true
        }

        val specificDevice : TwoStatePreference? = findPreference(SPECIFIC_DEVICE)
        specificDevice?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val bluetoothDevice : ATEListPreference? = findPreference(BLUETOOTH_DEVICE)
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val mBluetoothAdapter = bluetoothManager.adapter
        val address = ArrayList<String>()
        val name = ArrayList<String>()

        if (context?.let { ContextCompat.checkSelfPermission(it, BLUETOOTH_CONNECT) }
            == PERMISSION_GRANTED) {
            val pairedDevices = mBluetoothAdapter.bondedDevices
            for (bt in pairedDevices){
                address.add(bt.address)
            }

            for (bt in pairedDevices){
                name.add(bt.name)
            }
        }

        bluetoothDevice?.entries = name.toTypedArray()
        bluetoothDevice?.entryValues = address.toTypedArray()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_audio)

        val autoplays: PreferenceCategory? = findPreference("autoplays")
        autoplays?.isVisible = !PreferenceUtil.isSimpleMode
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val preference: Preference? = findPreference(AUTO_DOWNLOAD_IMAGES_POLICY)
        preference?.let { setSummary(it) }
    }
}
