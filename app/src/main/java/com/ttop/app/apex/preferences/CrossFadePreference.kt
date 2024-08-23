package com.ttop.app.apex.preferences

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.PreferenceDialogCrossFadeBinding
import com.ttop.app.apex.extensions.addAccentColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEDialogPreference


class CrossFadePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {
    init {
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            context.colorControlNormal(),
            SRC_IN
        )
    }
}

class CrossFadePreferenceDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = PreferenceDialogCrossFadeBinding.inflate(layoutInflater)

        binding.slider.apply {
            addAccentColor()

            if (PreferenceUtil.crossFadeDuration > 10) {
                PreferenceUtil.crossFadeDuration = 10
            }

            value = PreferenceUtil.crossFadeDuration.toFloat()
            updateText(value.toInt(), binding.duration)
            addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    updateText(value.toInt(), binding.duration)
                    if (!PreferenceUtil.isHapticFeedbackDisabled) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
            })
        }


        return materialDialog(R.string.cross_fade_duration)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> updateDuration(binding.slider.value.toInt()) }
            .setView(binding.root)
            .create()
            .centeredColorButtons()
    }

    private fun updateText(value: Int, duration: TextView) {
        var durationText = "$value s"
        val off = getString(R.string.off)
        if (value == 0) durationText += " / $off"
        duration.text = durationText
    }

    private fun updateDuration(duration: Int) {
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        PreferenceUtil.crossFadeDuration = duration
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        showToast(getString(R.string.warning_fade_cancelled))
                    }
                }
            }

        if (duration != 0) {
            val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
            builder?.setMessage(getString(R.string.warning_fade))?.setPositiveButton(getString(R.string.proceed), dialogClickListener)
                ?.setNegativeButton(getString(R.string.action_cancel), dialogClickListener)?.show()
        }else {
            PreferenceUtil.crossFadeDuration = duration
        }
    }

    companion object {
        fun newInstance(): CrossFadePreferenceDialog {
            return CrossFadePreferenceDialog()
        }
    }
}