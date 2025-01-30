/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ttop.app.apex.preferences

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceViewHolder
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ttop.app.apex.R
import com.ttop.app.apex.dialogs.BlacklistFolderChooserDialog
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.accentTextColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import com.ttop.app.apex.providers.BlacklistStore
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode
import java.io.File

class BlacklistPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1,
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        layoutResource = R.layout.custom_preference_no_summary
        icon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
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

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                    }
                    else -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.AUTO_BLACK -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                    }
                    else -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
            }
            ThemeMode.LIGHT -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
            }
            ThemeMode.MD3 -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
            }
        }
    }
}

class BlacklistPreferenceDialog : DialogFragment(), BlacklistFolderChooserDialog.FolderCallback {
    companion object {
        fun newInstance(): BlacklistPreferenceDialog {
            return BlacklistPreferenceDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val chooserDialog =
            childFragmentManager.findFragmentByTag("FOLDER_CHOOSER") as BlacklistFolderChooserDialog?
        chooserDialog?.setCallback(this)
        val context = requireActivity()

        refreshBlacklistData(context)

        val dialogTitle = TextView(requireContext())
        dialogTitle.text = ContextCompat.getString(requireContext(), R.string.blacklist)
        dialogTitle.setTextColor(accentColor())
        dialogTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        dialogTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER

        val dialogTitle1 = TextView(requireContext())
        dialogTitle1.text = ContextCompat.getString(requireContext(), R.string.clear_blacklist)
        dialogTitle1.setTextColor(accentColor())
        dialogTitle1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        dialogTitle1.textAlignment = View.TEXT_ALIGNMENT_CENTER

        val dialogTitle2 = TextView(requireContext())
        dialogTitle2.text = ContextCompat.getString(requireContext(), R.string.clear_blacklist)
        dialogTitle2.setTextColor(accentColor())
        dialogTitle2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        dialogTitle2.textAlignment = View.TEXT_ALIGNMENT_CENTER

        return materialDialog(R.string.blacklist)
            .setCustomTitle(dialogTitle)
            .setPositiveButton(R.string.done) { _, _ ->
                dismiss()
            }
            .setNeutralButton(R.string.clear_action) { _, _ ->
                materialDialog(R.string.clear_blacklist)
                    .setCustomTitle(dialogTitle1)
                    .setMessage(R.string.do_you_want_to_clear_the_blacklist)
                    .setPositiveButton(R.string.clear_action) { _, _ ->
                        BlacklistStore.getInstance(context).clear()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .centeredColorButtons()
                    .show()
            }
            .setNegativeButton(R.string.add_action) { _, _ ->
                val dialog = BlacklistFolderChooserDialog.create()
                dialog.setCallback(this@BlacklistPreferenceDialog)
                dialog.show(requireActivity().supportFragmentManager, "FOLDER_CHOOSER")
            }
            .setItems(paths.toTypedArray()) { _, which ->
                materialDialog(R.string.remove_from_blacklist)
                    .setCustomTitle(dialogTitle2)
                    .setMessage(
                        String.format(
                            getString(R.string.do_you_want_to_remove_from_the_blacklist),
                            paths[which]
                        ).parseAsHtml()
                    )
                    .setPositiveButton(R.string.remove_action) { _, _ ->
                        BlacklistStore.getInstance(context).removePath(File(paths[which]))
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .centeredColorButtons()
                    .show()
            }
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).accentTextColor()
                    getButton(AlertDialog.BUTTON_NEGATIVE).accentTextColor()
                    getButton(AlertDialog.BUTTON_NEUTRAL).accentTextColor()
                    withCenteredButtons()
                }
            }
    }

    private lateinit var paths: ArrayList<String>

    private fun refreshBlacklistData(context: Context?) {
        if (context == null) return
        this.paths = BlacklistStore.getInstance(context).paths
        val dialog = dialog as MaterialAlertDialogBuilder?
        dialog?.setItems(paths.toTypedArray(), null)

    }

    override fun onFolderSelection(context: Context, folder: File) {
        BlacklistStore.getInstance(context).addPath(folder)
    }
}
