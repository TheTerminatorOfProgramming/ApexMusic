package com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7

import android.content.Context
import android.util.AttributeSet
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.preference.CheckBoxPreference
import com.ttop.app.apex.R
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil

/**
 * @author Aidan Follestad (afollestad)
 */
class ATESwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) :
    CheckBoxPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        layoutResource = R.layout.custom_preference
        widgetLayoutResource = R.layout.ate_preference_switch_support
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ATHUtil.resolveColor(
                context,
                android.R.attr.colorControlNormal
            ), BlendModeCompat.SRC_IN
        )
    }
}