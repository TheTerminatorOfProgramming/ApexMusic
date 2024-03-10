package com.ttop.app.apex.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.DialogPlaybackSpeedBinding
import com.ttop.app.apex.extensions.accent
import com.ttop.app.apex.extensions.accentTextColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.util.PreferenceUtil

class PlaybackSpeedDialog : DialogFragment() {

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null
    private var dismissButton: Button? = null
    private lateinit var dialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogPlaybackSpeedBinding.inflate(layoutInflater)
        binding.playbackSpeedSlider.accent()
        binding.playbackPitchSlider.accent()
        binding.playbackSpeedSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            binding.speedValue.text = "$value"
        })
        binding.playbackPitchSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            binding.pitchValue.text = "$value"
        })
        binding.playbackSpeedSlider.value = PreferenceUtil.playbackSpeed
        binding.playbackPitchSlider.value = PreferenceUtil.playbackPitch

        materialDialog(R.string.playback_settings).apply {
            setPositiveButton(R.string.save) { _, _ ->
                dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                updatePlaybackAndPitch(
                    binding.playbackSpeedSlider.value,
                    binding.playbackPitchSlider.value
                )
            }

            setNegativeButton(R.string.dismiss) {_, _ ->
                dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                dialog.dismiss()
            }

            setNeutralButton(R.string.reset_action) {_, _ ->
                dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    updatePlaybackAndPitch(
                        1F,
                        1F
                    )
                }
            setView(binding.root)
            dialog = create()
            dialog.centeredColorButtons()
            dialog.setCanceledOnTouchOutside(false)
        }
        return dialog
    }

    private fun updatePlaybackAndPitch(speed: Float, pitch: Float) {
        PreferenceUtil.playbackSpeed = speed
        PreferenceUtil.playbackPitch = pitch
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            positiveButton = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            negativeButton = d.getButton(Dialog.BUTTON_NEGATIVE) as Button
            dismissButton = d.getButton(Dialog.BUTTON_NEUTRAL) as Button

            positiveButton!!.accentTextColor()
            negativeButton!!.accentTextColor()
            dismissButton!!.accentTextColor()

            d.withCenteredButtons()
        }
    }

    companion object {
        fun newInstance(): PlaybackSpeedDialog {
            return PlaybackSpeedDialog()
        }
    }
}