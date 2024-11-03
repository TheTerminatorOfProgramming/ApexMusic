package com.ttop.app.apex.preferences

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.PreferenceDialogSeekBinding
import com.ttop.app.apex.extensions.addAccentColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import com.ttop.app.apex.util.PreferenceUtil


class SeekFwdPreference @JvmOverloads constructor(
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

class SeekFwdPreferenceDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = PreferenceDialogSeekBinding.inflate(layoutInflater)

        binding.slider.apply {
            addAccentColor()
            value = PreferenceUtil.fastForwardDuration.toFloat()
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


        return materialDialog(R.string.pref_title_fast_forward_duration)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> updateDuration(binding.slider.value.toInt()) }
            .setView(binding.root)
            .create()
            .centeredColorButtons()
    }

    private fun updateText(value: Int, duration: TextView) {
        val durationText = "$value s"
        duration.text = durationText
    }

    private fun updateDuration(duration: Int) {
        PreferenceUtil.fastForwardDuration = duration
    }

    companion object {
        fun newInstance(): SeekFwdPreferenceDialog {
            return SeekFwdPreferenceDialog()
        }
    }
}