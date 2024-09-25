package com.ttop.app.apex.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.DialogVolumeBinding
import com.ttop.app.apex.extensions.accent
import com.ttop.app.apex.extensions.accentTextColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.util.PreferenceUtil


class VolumeDialog : DialogFragment() {

    private var positiveButton: Button? = null
    private var resetButton: Button? = null
    private lateinit var dialog: AlertDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogVolumeBinding.inflate(layoutInflater)
        val am: AudioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager
        val volumeLevel: Int = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolumeLevel: Int = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        binding.volumeSlider.valueTo = maxVolumeLevel.toFloat()
        binding.volumeSlider.value = volumeLevel.toFloat()
        binding.volumeValue.text = "${volumeLevel * 10}%"

        binding.volumeSlider.accent()
        binding.volumeSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            am.setStreamVolume(AudioManager.STREAM_MUSIC, value.toInt(), 0)
            binding.volumeValue.text = "${(value * 10).toInt()}%"
        })

        if (volumeLevel > maxVolumeLevel) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumeLevel, 0)
            binding.volumeSlider.value = maxVolumeLevel.toFloat()
            binding.volumeValue.text = "${volumeLevel * 10}%"
        }

        materialDialog(R.string.volume).apply {
            setPositiveButton(R.string.save) { _, _ ->
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }

                am.setStreamVolume(AudioManager.STREAM_MUSIC, binding.volumeSlider.value.toInt(), 0)
            }

            setNegativeButton(R.string.reset_action) {_, _ ->
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                dialog.dismiss()
            }

            setView(binding.root)
            dialog = create()
            dialog.centeredColorButtons()
            dialog.setCanceledOnTouchOutside(false)

            dialog.setOnShowListener {
                resetButton?.setOnClickListener {
                    if (!PreferenceUtil.isHapticFeedbackDisabled) {
                        dialog.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }

                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (5), 0)

                    binding.volumeSlider.value = (5).toFloat()
                    binding.volumeValue.text = "${5 * 10}%"
                }
            }
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            positiveButton = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            resetButton = d.getButton(Dialog.BUTTON_NEGATIVE) as Button

            positiveButton!!.accentTextColor()
            resetButton!!.accentTextColor()

            d.withCenteredButtons()
        }
    }

    companion object {
        fun newInstance(): VolumeDialog {
            return VolumeDialog()
        }
    }
}