package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R

class BluetoothAutoPlaySlideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_bluetooth_autoplay_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), com.ttop.app.appthemehelper.R.color.md_green_500))
    }
    companion object {
        fun newInstance(): BluetoothAutoPlaySlideFragment {
            return BluetoothAutoPlaySlideFragment()
        }
    }
}