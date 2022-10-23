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
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.*
import com.ttop.app.apex.ui.activities.base.AbsBaseActivity.Companion.BLUETOOTH_PERMISSION_REQUEST
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.appthemehelper.util.VersionUtils


/**
 * @author Hemanth S (h4h13).
 */

class AudioSettings : AbsSettingsFragment() {
    override fun invalidateSettings() {
        val eqPreference: Preference? = findPreference(EQUALIZER)
        if (!hasEqualizer()) {
            eqPreference?.isEnabled = false
            eqPreference?.summary = resources.getString(R.string.no_equalizer)
        } else {
            eqPreference?.isEnabled = true
        }
        eqPreference?.setOnPreferenceClickListener {
            NavigationUtil.openEqualizer(requireActivity())
            true
        }

        val pause: TwoStatePreference? = findPreference(PAUSE_ON_ZERO_VOLUME)
        pause?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val reduce: TwoStatePreference? = findPreference(AUDIO_DUCKING)
        reduce?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val manageAudio: TwoStatePreference? = findPreference(MANAGE_AUDIO_FOCUS)
        manageAudio?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val gapless: TwoStatePreference? = findPreference(GAP_LESS_PLAYBACK)
        gapless?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val autoplay: TwoStatePreference? = findPreference(TOGGLE_HEADSET)
        autoplay?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val bluetoothPreference: Preference? = findPreference(BLUETOOTH_PLAYBACK)
        if (VersionUtils.hasS()) {
            bluetoothPreference?.setOnPreferenceChangeListener { _, newValue ->
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
        }

        val specific_device : TwoStatePreference? = findPreference(SPECIFIC_DEVICE)
        specific_device?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        val bluetooth_device : ATEListPreference? = findPreference(BLUETOOTH_DEVICE)
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val mBluetoothAdapter = bluetoothManager.adapter
        val address = ArrayList<String>()
        val name = ArrayList<String>()

        if (VersionUtils.hasS()) {
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
        }

        bluetooth_device?.entries = name.toTypedArray()
        bluetooth_device?.entryValues = address.toTypedArray()

        val bt_volume: TwoStatePreference? = findPreference(BT_VOLUME)
        bt_volume?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    private fun hasEqualizer(): Boolean {
        val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)

        val pm = requireActivity().packageManager
        val ri = pm.resolveActivity(effects, 0)
        return ri != null
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if(PreferenceUtil.isUiMode == "full") {
            if (PreferenceUtil.isSamsungSoundPluginInstalled) {
                addPreferencesFromResource(R.xml.pref_audio_samsung)
            }else {
                addPreferencesFromResource(R.xml.pref_audio)
            }
        }else {
            addPreferencesFromResource(R.xml.pref_audio_lite)
        }
    }
}
