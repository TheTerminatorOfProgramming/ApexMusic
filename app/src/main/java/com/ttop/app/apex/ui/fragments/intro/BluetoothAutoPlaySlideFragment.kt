package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.github.appintro.SlidePolicy
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast

class BluetoothAutoPlaySlideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_bluetooth_autoplay_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(resources.getColor(R.color.md_cyan_400))
    }
    companion object {
        fun newInstance(fragmentRingtoneIntro: Int): BluetoothAutoPlaySlideFragment {
            return BluetoothAutoPlaySlideFragment()
        }
    }
}