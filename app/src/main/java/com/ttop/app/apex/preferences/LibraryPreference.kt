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
import android.content.DialogInterface
import android.os.Bundle
import android.util.AttributeSet
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.CategoryInfoAdapter
import com.ttop.app.apex.databinding.PreferenceDialogLibraryCategoriesBinding
import com.ttop.app.apex.extensions.colorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.model.CategoryInfo
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEDialogPreference


class LibraryPreference @JvmOverloads constructor(
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

class LibraryPreferenceDialog : DialogFragment() {
    var positiveBtnClicked = false
    var wasOne = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = PreferenceDialogLibraryCategoriesBinding.inflate(layoutInflater)

        val categoryAdapter = CategoryInfoAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = categoryAdapter
            categoryAdapter.attachToRecyclerView(this)
        }

        return materialDialog(R.string.library_categories)
            .setNeutralButton(
                R.string.reset_action
            ) { _, _ ->
                if (PreferenceUtil.isUiMode == "full") {
                    updateCategories(PreferenceUtil.defaultCategories)
                }else {
                    updateCategories(PreferenceUtil.defaultCategoriesLite)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.done) { _, _ ->
                wasOne = getSelected(PreferenceUtil.libraryCategory)
                updateCategories(categoryAdapter.categoryInfos)
                positiveBtnClicked = true
                PreferenceUtil.shouldRecreateTabs = true
            }
            .setView(binding.root)
            .create()
            .colorButtons()
    }

    private fun updateCategories(categories: List<CategoryInfo>) {
        if (getSelected(categories) == 0) return
        if (PreferenceUtil.isUiMode == "full") {
            if (getSelected(categories) > 5) {
                showToast(R.string.message_limit_tabs)
                return
            }
        }else {
            if (getSelected(categories) > 4) {
                showToast(R.string.message_limit_lite_tabs)
                return
            }
        }

        PreferenceUtil.libraryCategory = categories

        PreferenceUtil.tempValue = getSelected(categories)
    }

    private fun getSelected(categories: List<CategoryInfo>): Int {
        var selected = 0
        for (categoryInfo in categories) {
            if (categoryInfo.visible)
                selected++
        }
        return selected
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (positiveBtnClicked && wasOne == 1){
            positiveBtnClicked = false
            activity?.recreate()
        }

        if (positiveBtnClicked && PreferenceUtil.tempValue == 1){
            positiveBtnClicked = false
            activity?.recreate()
        }
    }

    companion object {
        fun newInstance(): LibraryPreferenceDialog {
            return LibraryPreferenceDialog()
        }
    }
}