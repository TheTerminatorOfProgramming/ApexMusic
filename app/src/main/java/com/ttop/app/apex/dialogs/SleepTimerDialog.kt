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

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import com.google.android.material.textview.MaterialTextView
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.DialogSleepTimerBinding
import com.ttop.app.apex.extensions.accentTextColor
import com.ttop.app.apex.extensions.addAccentColor
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.ACTION_PENDING_QUIT
import com.ttop.app.apex.service.MusicService.Companion.ACTION_QUIT
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil


class SleepTimerDialog : DialogFragment() {

    private var seekArcProgress: Int = 0
    private lateinit var timerUpdater: TimerUpdater

    private var _binding: DialogSleepTimerBinding? = null
    private val binding get() = _binding!!

    private val shouldFinishLastSong: CheckBox get() = binding.shouldFinishLastSong
    private val seekBar: SeekBar get() = binding.seekBar
    private val timerDisplay: TextView get() = binding.timerDisplay
    private val remaining: MaterialTextView get() = binding.remainingTime

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null
    private var dismissButton: Button? = null
    private lateinit var dialog: AlertDialog

    @SuppressLint("DefaultLocale")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        timerUpdater = TimerUpdater()
        _binding = DialogSleepTimerBinding.inflate(layoutInflater)

        //val defaultText = getString(R.string.remaining) + ":"

        //binding.remainingTime.text = defaultText

        val finishMusic = PreferenceUtil.isSleepTimerFinishMusic

        shouldFinishLastSong.apply {
            addAccentColor()
            isChecked = finishMusic
        }

        seekBar.apply {
            addAccentColor()
            seekArcProgress = PreferenceUtil.lastSleepTimerValue
            updateTimeDisplayTime()
            progress = seekArcProgress
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i < 1) {
                    seekBar.progress = 1
                    return
                }
                seekArcProgress = i

                if (!PreferenceUtil.isTimerCancelled && seekArcProgress == PreferenceUtil.lastSleepTimerValue) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.reset_action)
                }else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.action_set)
                }
                updateTimeDisplayTime()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                PreferenceUtil.lastSleepTimerValue = seekArcProgress
            }
        })

        binding.shouldFinishLastSong.setOnClickListener {
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }

        materialDialog(R.string.action_sleep_timer).apply {
            timerUpdater.start()
            setPositiveButton(R.string.action_set) { _, _ ->
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                PreferenceUtil.isSleepTimerFinishMusic = shouldFinishLastSong.isChecked
                val minutes = seekArcProgress
                val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
                val nextSleepTimerElapsedTime =
                    SystemClock.elapsedRealtime() + minutes * 60 * 1000
                PreferenceUtil.nextSleepTimerElapsedRealTime = nextSleepTimerElapsedTime.toInt()
                val am = requireContext().getSystemService<AlarmManager>()
                if (am?.canScheduleExactAlarms() == true) {
                    am.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextSleepTimerElapsedTime,
                        pi
                    )
                }

                showToast(String.format(getString(R.string.sleep_timer_set), minutes))
                PreferenceUtil.isTimerCancelled = false
            }

            setNegativeButton(R.string.action_cancel) { _, _ ->
                timerUpdater.cancel()
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
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
                PreferenceUtil.isTimerCancelled = true
            }

            setNeutralButton(R.string.dismiss) { _, _ ->
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                dialog.dismiss()
            }
            setView(binding.root)
            dialog = create()
            dialog.setCanceledOnTouchOutside(false)
        }
        return dialog
    }

    private fun updateTimeDisplayTime() {
        val minutes = "$seekArcProgress min"
        timerDisplay.text = minutes
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent {
        return PendingIntent.getService(
            requireActivity(), 0, makeTimerIntent(), flag or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(requireActivity(), MusicService::class.java)
        return if (shouldFinishLastSong.isChecked) {
            intent.setAction(ACTION_PENDING_QUIT)
        } else intent.setAction(ACTION_QUIT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        timerUpdater.cancel()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            positiveButton = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            negativeButton = d.getButton(Dialog.BUTTON_NEGATIVE) as Button
            dismissButton = d.getButton(Dialog.BUTTON_NEUTRAL) as Button
            if (PreferenceUtil.isTimerCancelled) {
                negativeButton!!.isEnabled = false
            }
            positiveButton!!.accentTextColor()
            if (!PreferenceUtil.isTimerCancelled) {
                negativeButton!!.accentTextColor()
            }

            dismissButton!!.accentTextColor()

            positiveButton!!.textSize = 13f
            negativeButton!!.textSize = 13f
            dismissButton!!.textSize = 13f
        }
    }

    private inner class TimerUpdater :
        CountDownTimer(
            PreferenceUtil.nextSleepTimerElapsedRealTime - SystemClock.elapsedRealtime(),
            1000
        ) {

        override fun onTick(millisUntilFinished: Long) {
            if (PreferenceUtil.isTimerCancelled) {
                super.cancel()
            }else {
                val remainingTime = "${getString(R.string.remaining)}: ${MusicUtil.getReadableDurationString(millisUntilFinished)}"
                remaining.text =  remainingTime
            }
        }

        override fun onFinish() {
            val defaultText = getString(R.string.remaining) + ":"
            remaining.text = defaultText
        }
    }
}