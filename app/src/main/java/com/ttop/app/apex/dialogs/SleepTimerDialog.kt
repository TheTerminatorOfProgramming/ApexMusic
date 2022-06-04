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
package com.ttop.app.apex.dialogs

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.DialogSleepTimerBinding
import com.ttop.app.apex.extensions.addAccentColor
import com.ttop.app.apex.extensions.colorButtons
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.ACTION_PENDING_QUIT
import com.ttop.app.apex.service.MusicService.Companion.ACTION_QUIT
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.util.VersionUtils
import java.util.concurrent.atomic.AtomicBoolean


class SleepTimerDialog : DialogFragment() {

    private var seekArcProgress: Int = 0
    private lateinit var timerUpdater: TimerUpdater
    private lateinit var dialog: MaterialDialog
    private lateinit var shouldFinishLastSong: CheckBox
    private lateinit var timerDisplay: EditText
    private val changingText: AtomicBoolean = AtomicBoolean(false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        timerUpdater = TimerUpdater()
        val binding = DialogSleepTimerBinding.inflate(layoutInflater)
        shouldFinishLastSong = binding.shouldFinishLastSong
        timerDisplay = binding.timerDisplay

        val finishMusic = PreferenceUtil.isSleepTimerFinishMusic
        shouldFinishLastSong.apply {
            addAccentColor()
            isChecked = finishMusic
        }
        binding.seekBar.apply {
            addAccentColor()
            seekArcProgress = PreferenceUtil.lastSleepTimerValue
            updateTimeDisplayTime()
            progress = seekArcProgress
        }


        timerDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (changingText.get()) {
                    return
                }
                changingText.set(true);
                var value = s.toString()
                if (value.isEmpty()) {
                    value = "1";
                }
                binding.seekBar.progress = value.toInt()
                seekArcProgress = value.toInt()
                PreferenceUtil.lastSleepTimerValue = seekArcProgress
                changingText.set(false);
            }
        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i < 1) {
                    seekBar.progress = 1
                    return
                }
                seekArcProgress = i
                if (!changingText.get()) {
                    updateTimeDisplayTime();
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                PreferenceUtil.lastSleepTimerValue = seekArcProgress
            }
        })
        return materialDialog(R.string.action_sleep_timer)
            .setView(binding.root)
            .setPositiveButton(R.string.action_set) { _, _ ->
                PreferenceUtil.isSleepTimerFinishMusic = shouldFinishLastSong.isChecked

                if (timerDisplay.text.isEmpty()){
                    seekArcProgress = 1
                    updateTimeDisplayTime()
                }

                val minutes = seekArcProgress
                val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
                val nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + minutes * 60 * 1000
                PreferenceUtil.nextSleepTimerElapsedRealTime = nextSleepTimerElapsedTime.toInt()
                val am = requireContext().getSystemService<AlarmManager>()
                am?.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime, pi)

                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.sleep_timer_set, minutes),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
                if (previous != null) {
                    val am = requireContext().getSystemService<AlarmManager>()
                    am?.cancel(previous)
                    previous.cancel()
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.sleep_timer_canceled),
                        Toast.LENGTH_SHORT
                    ).show()
                    val musicService = MusicPlayerRemote.musicService
                    if (musicService != null && musicService.pendingQuit) {
                        musicService.pendingQuit = false
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.sleep_timer_canceled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .create()
            .colorButtons()
    }

    private fun updateTimeDisplayTime() {
        val seek = "$seekArcProgress"
        timerDisplay.text = seek.toEditable()
    }

    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(
            requireActivity(), 0, makeTimerIntent(), flag or if (VersionUtils.hasMarshmallow())
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(requireActivity(), MusicService::class.java)
        return if (shouldFinishLastSong.isChecked) {
            intent.setAction(ACTION_PENDING_QUIT)
        } else intent.setAction(ACTION_QUIT)
    }

    private fun updateCancelButton() {
        val musicService = MusicPlayerRemote.musicService
        if (musicService != null && musicService.pendingQuit) {
            dialog.getActionButton(WhichButton.NEUTRAL).text =
                dialog.context.getString(R.string.cancel_current_timer)
        } else {
            dialog.getActionButton(WhichButton.NEUTRAL).text = null
        }
    }

    private inner class TimerUpdater :
        CountDownTimer(
            PreferenceUtil.nextSleepTimerElapsedRealTime - SystemClock.elapsedRealtime(),
            1000
        ) {

        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            updateCancelButton()
        }
    }
}
