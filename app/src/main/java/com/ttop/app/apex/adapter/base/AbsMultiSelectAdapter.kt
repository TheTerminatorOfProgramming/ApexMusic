package com.ttop.app.apex.adapter.base

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.view.iterator
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.NumberRollViewBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.extensions.m3BgaccentColor
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.extensions.tint
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode
import com.ttop.app.apex.views.NumberRollView

abstract class AbsMultiSelectAdapter<V : RecyclerView.ViewHolder?, I>(
    open val activity: FragmentActivity, @MenuRes menuRes: Int,
) : RecyclerView.Adapter<V>(), ActionMode.Callback {
    var actionMode: ActionMode? = null
    private val checked: MutableList<I>
    private var menuRes: Int
    private val mainActivity get() = activity as MainActivity

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val inflater = mode?.menuInflater
        inflater?.inflate(menuRes, menu)
        activity.window.statusBarColor = activity.surfaceColor()
        for (item in menu!!.iterator()){
            val title = item.icon
            when (PreferenceUtil.getGeneralThemeValue()) {
                ThemeMode.AUTO -> {
                    when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                ContextCompat.getColor(activity, R.color.md_white_1000),
                                SRC_IN
                            )
                        }

                        Configuration.UI_MODE_NIGHT_NO,
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                ContextCompat.getColor(activity, R.color.darkColorSurface),
                                SRC_IN
                            )
                        }

                        else -> {
                            title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                ContextCompat.getColor(activity, R.color.md_white_1000),
                                SRC_IN
                            )
                        }
                    }
                }

                ThemeMode.AUTO_BLACK -> {
                    when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                ContextCompat.getColor(activity, R.color.md_white_1000),
                                SRC_IN
                            )
                        }

                        Configuration.UI_MODE_NIGHT_NO,
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                ContextCompat.getColor(activity, R.color.blackColorSurface),
                                SRC_IN
                            )
                        }

                        else -> {
                            title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                ContextCompat.getColor(activity, R.color.md_white_1000),
                                SRC_IN
                            )
                        }
                    }
                }

                ThemeMode.BLACK,
                ThemeMode.DARK -> {
                    title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        ContextCompat.getColor(activity, R.color.md_white_1000),
                        SRC_IN
                    )
                }

                ThemeMode.LIGHT -> {
                    title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        ContextCompat.getColor(activity, R.color.darkColorSurface),
                        SRC_IN
                    )
                }

                ThemeMode.MD3 -> {
                    title?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        activity.accentColor(),
                        SRC_IN
                    )
                }
            }
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_multi_select_adapter_check_all) {
            checkAll()
        } else {
            onMultipleItemAction(item!!, ArrayList(checked))
            actionMode?.finish()
            clearChecked()
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        clearChecked()
        actionMode = null
        //activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.statusBarColor =if (PreferenceUtil.getGeneralThemeValue() == ThemeMode.MD3) {
             activity.m3BgaccentColor()
        }else {
            activity.surfaceColor()
        }

        onBackPressedCallback.remove()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun checkAll() {
        if (actionMode != null) {
            checked.clear()
            for (i in 0 until itemCount) {
                val identifier = getIdentifier(i)
                if (identifier != null) {
                    checked.add(identifier)
                }
            }
            notifyDataSetChanged()
            updateCab()
        }
    }

    protected abstract fun getIdentifier(position: Int): I?

    protected abstract fun getName(model: I): String?

    protected fun isChecked(identifier: I): Boolean {
        return checked.contains(identifier)
    }

    protected val isInQuickSelectMode: Boolean
        get() = actionMode != null

    protected abstract fun onMultipleItemAction(menuItem: MenuItem, selection: List<I>)
    protected fun setMultiSelectMenuRes(@MenuRes menuRes: Int) {
        this.menuRes = menuRes
    }

    protected fun toggleChecked(position: Int): Boolean {
        val identifier = getIdentifier(position) ?: return false
        if (!checked.remove(identifier)) {
            checked.add(identifier)
        }
        notifyItemChanged(position)
        updateCab()
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearChecked() {
        checked.clear()
        notifyDataSetChanged()
    }

    private fun updateCab() {
        if (actionMode == null) {
            actionMode = activity.startActionMode(this)?.apply {
                customView = NumberRollViewBinding.inflate(activity.layoutInflater).root
            }
            activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
        }
        val size = checked.size
        when {
            size <= 0 -> {
                actionMode?.finish()
            }

            else -> {
                actionMode?.customView?.findViewById<NumberRollView>(R.id.selection_mode_number)
                    ?.setNumber(size, true)
            }
        }
    }

    init {
        checked = ArrayList()
        this.menuRes = menuRes

    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (actionMode != null) {
                actionMode?.finish()
                remove()
            }
        }
    }
}