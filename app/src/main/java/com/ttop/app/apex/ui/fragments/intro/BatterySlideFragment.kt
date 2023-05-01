package com.ttop.app.apex.ui.fragments.intro

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.appintro.SlidePolicy

class BatterySlideFragment : Fragment(), SlidePolicy {

    private lateinit var battery: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_battery_intro, container, false)

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        battery = view.findViewById(R.id.permission_battery) as Button

        if (!ApexUtil.hasBatteryPermission()) {
            battery.text = "Disable Battery Optimization"
        }else {
            battery.text = "Battery Optimization Already Disabled!"
        }

        battery.setOnClickListener {
           ApexUtil.disableBatteryOptimization()
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_deep_purple_400))
    }

    override val isPolicyRespected: Boolean
        @RequiresApi(Build.VERSION_CODES.S)
        get() = ApexUtil.hasBatteryPermission()

    override fun onUserIllegallyRequestedNextPage() {

    }

    companion object {
        fun newInstance(): BatterySlideFragment {
            return BatterySlideFragment()
        }
    }
}