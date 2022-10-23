package com.ttop.app.apex.ui.fragments.intro

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.AppWidgetBig
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetFullCircle
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ThemeStore


class AppModeSlideFragment : Fragment() {

    private lateinit var liteMode: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_app_mode_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liteMode = view.findViewById(R.id.appMode) as SwitchCompat

        liteMode.setOnCheckedChangeListener { _, isChecked ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val packageManager: PackageManager? = context?.packageManager
            if (isChecked) {
                PreferenceUtil.isUiMode = "lite"
                context?.let {
                    ComponentName(
                        it,
                        AppWidgetBig::class.java
                    )
                }?.let {
                    packageManager!!.setComponentEnabledSetting(
                        it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                    )
                }

                context?.let {
                    ComponentName(
                        it,
                        AppWidgetCircle::class.java
                    )
                }?.let {
                    packageManager!!.setComponentEnabledSetting(
                        it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                    )
                }

                context?.let {
                    ComponentName(
                        it,
                        AppWidgetFullCircle::class.java
                    )
                }?.let {
                    packageManager!!.setComponentEnabledSetting(
                        it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                    )
                }
            } else {
                PreferenceUtil.isUiMode = "full"
                context?.let {
                    ComponentName(
                        it,
                        AppWidgetBig::class.java
                    )
                }?.let {
                    packageManager!!.setComponentEnabledSetting(
                        it, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                    )
                }

                context?.let {
                    ComponentName(
                        it,
                        AppWidgetCircle::class.java
                    )
                }?.let {
                    packageManager!!.setComponentEnabledSetting(
                        it, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                    )
                }

                context?.let {
                    ComponentName(
                        it,
                        AppWidgetFullCircle::class.java
                    )
                }?.let {
                    packageManager!!.setComponentEnabledSetting(
                        it, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                    )
                }
            }
            PreferenceUtil.shouldRecreate = true
        }

        view.setBackgroundColor(resources.getColor(R.color.md_grey_500))
    }
    companion object {
        fun newInstance(fragmentRingtoneIntro: Int): AppModeSlideFragment {
            return AppModeSlideFragment()
        }
    }
}