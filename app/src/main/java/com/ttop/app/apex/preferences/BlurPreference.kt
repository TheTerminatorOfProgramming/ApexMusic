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
import com.ttop.app.apex.databinding.PreferenceDialogBlurBinding
import com.ttop.app.apex.extensions.addAccentColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEDialogPreference


class BlurPreference @JvmOverloads constructor(
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

class BlurPreferenceDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = PreferenceDialogBlurBinding.inflate(layoutInflater)

        binding.slider.apply {
            addAccentColor()
            value = PreferenceUtil.blurAmount.toFloat()
            updateText(value.toInt(), binding.blur)
            addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    updateText(value.toInt(), binding.blur)
                    if (!PreferenceUtil.isHapticFeedbackDisabled) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
            })
        }


        return materialDialog(R.string.pref_blur_amount_title)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> updateBlur(binding.slider.value.toInt()) }
            .setView(binding.root)
            .create()
            .centeredColorButtons()
    }

    private fun updateText(value: Int, blur: TextView) {
        val durationText = "$value"
        blur.text = durationText
    }

    private fun updateBlur(blur: Int) {
        PreferenceUtil.blurAmount = blur
    }

    companion object {
        fun newInstance(): BlurPreferenceDialog {
            return BlurPreferenceDialog()
        }
    }
}