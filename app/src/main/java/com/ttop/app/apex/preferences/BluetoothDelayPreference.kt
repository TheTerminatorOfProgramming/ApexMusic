package com.ttop.app.apex.preferences

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceViewHolder
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.PreferenceDialogBluetoothDelayBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.addAccentColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode


class BluetoothDelayPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {
    init {
        layoutResource = R.layout.custom_preference
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            context.colorControlNormal(),
            SRC_IN
        )
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val cardview = holder.itemView.findViewById<MaterialCardView>(R.id.listCard)
        cardview?.strokeColor = com.ttop.app.apex.libraries.appthemehelper.ThemeStore.accentColor(context)
        cardview?.setBackgroundColor(context.surfaceColor())

        val title = holder.itemView.findViewById<TextView>(android.R.id.title)
        val summary = holder.itemView.findViewById<TextView>(android.R.id.summary)

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                    }
                    else -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.AUTO_BLACK -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                    }
                    else -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
            }
            ThemeMode.LIGHT -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
            }
            ThemeMode.MD3 -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
                summary.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
            }
        }
    }
}

class BluetoothDelayPreferenceDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = PreferenceDialogBluetoothDelayBinding.inflate(layoutInflater)

        binding.slider.apply {
            addAccentColor()

            value = (PreferenceUtil.bluetoothDelay / 1000).toFloat()
            updateText(value.toInt(), binding.delay)
            addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    updateText(value.toInt(), binding.delay)
                    if (!PreferenceUtil.isHapticFeedbackDisabled) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
            })
        }

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.delay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.md_white_1000
                            )
                        )
                    }

                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.delay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.darkColorSurface
                            )
                        )
                    }

                    else -> {
                        binding.delay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.md_white_1000
                            )
                        )
                    }
                }
            }

            ThemeMode.AUTO_BLACK -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.delay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.md_white_1000
                            )
                        )
                    }

                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.delay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.blackColorSurface
                            )
                        )
                    }

                    else -> {
                        binding.delay.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.md_white_1000
                            )
                        )
                    }
                }
            }

            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                binding.delay.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
            }

            ThemeMode.LIGHT -> {
                binding.delay.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
            }

            ThemeMode.MD3 -> {
                binding.delay.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.m3_widget_other_text
                    )
                )
            }
        }

        val dialogTitle = TextView(requireContext())
        dialogTitle.text = ContextCompat.getString(requireContext(), R.string.bluetooth_delay)
        dialogTitle.setTextColor(accentColor())
        dialogTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        dialogTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER

        return materialDialog(R.string.bluetooth_delay)
            .setCustomTitle(dialogTitle)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> updateDelay(binding.slider.value.toInt()) }
            .setView(binding.root)
            .create()
            .centeredColorButtons()
    }

    private fun updateText(value: Int, delay: TextView) {
        val durationText = "$value s"
        delay.text = durationText
    }

    private fun updateDelay(delay: Int) {
        val delayMillis = delay * 1000
        PreferenceUtil.bluetoothDelay = delayMillis
    }

    companion object {
        fun newInstance(): BluetoothDelayPreferenceDialog {
            return BluetoothDelayPreferenceDialog()
        }
    }
}