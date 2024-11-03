package com.ttop.app.apex.libraries.appthemehelper.common.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.google.android.material.materialswitch.MaterialSwitch
import com.ttop.app.apex.libraries.appthemehelper.ATH

/**
 * @author Aidan Follestad (afollestad)
 */
class ATESwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
) : MaterialSwitch(context, attrs, defStyleAttr) {

    init {
        if (!isInEditMode) {
            ATH.setTint(
                this,
                com.ttop.app.apex.libraries.appthemehelper.ThemeStore.accentColor(context)
            )
        }
    }

    override fun isShown(): Boolean {
        return parent != null && isVisible
    }
}