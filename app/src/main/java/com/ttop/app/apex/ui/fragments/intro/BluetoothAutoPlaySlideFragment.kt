package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.libraries.appintro.SlideBackgroundColorHolder

class BluetoothAutoPlaySlideFragment(
    override val defaultBackgroundColorRes: Int
) : Fragment(), SlideBackgroundColorHolder {

    //Required Constructor
    constructor() : this(R.color.md_green_500)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_bluetooth_autoplay, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_green_500))
    }

    companion object {
        fun newInstance(): BluetoothAutoPlaySlideFragment {
            return BluetoothAutoPlaySlideFragment(R.color.md_green_500)
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }
}