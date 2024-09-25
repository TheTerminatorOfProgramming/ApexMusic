package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.appintro.SlideBackgroundColorHolder
import com.ttop.app.appintro.SlidePolicy

class BatterySlideFragment(
    override val defaultBackgroundColorRes: Int
) : Fragment(), SlidePolicy, SlideBackgroundColorHolder {

    private lateinit var battery: Button
    val handler = Handler(Looper.getMainLooper())
    private var isLooping = true

    private val runnable: Runnable = Runnable {
        if (isLooping) {
            startLooping()
        }
    }

    //Required Constructor
    constructor() : this(com.ttop.app.appthemehelper.R.color.md_deep_purple_400)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_battery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        battery = view.findViewById(R.id.permission_battery) as Button

        if (!ApexUtil.hasBatteryPermission()) {
            battery.text = activity?.getString(R.string.disable_battery_optimize)

            battery.setOnClickListener {
                ApexUtil.disableBatteryOptimization()
            }
        }else {
            battery.text = activity?.getString(R.string.battery_optimize)
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), com.ttop.app.appthemehelper.R.color.md_deep_purple_400))

        if (!ApexUtil.hasBatteryPermission()) {
            if (isLooping) {
                handler.postDelayed(runnable,250)
            }
        }
    }

    private fun startLooping() {
        if (ApexUtil.hasBatteryPermission()) {
            isLooping = false
            battery.text = activity?.getString(R.string.battery_optimize_disabled)
        }
        handler.postDelayed(runnable, 250)
    }

    override val isPolicyRespected: Boolean
        get() = ApexUtil.hasBatteryPermission()

    override fun onUserIllegallyRequestedNextPage() {

    }

    companion object {
        fun newInstance(): BatterySlideFragment {
            return BatterySlideFragment(com.ttop.app.appthemehelper.R.color.md_deep_purple_400)
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }
}