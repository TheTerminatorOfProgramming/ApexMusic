package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.util.IntroPrefs
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appintro.SlideBackgroundColorHolder

class ShuffleSlideFragment(
    override val defaultBackgroundColorRes: Int
) : Fragment(), SlideBackgroundColorHolder {

    private lateinit var shuffleMode: SwitchCompat

    //Required Constructor
    constructor() : this(R.color.md_indigo_A400)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_shuffle, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shuffleMode = view.findViewById(R.id.shuffleMode) as SwitchCompat

        shuffleMode.isChecked = IntroPrefs(requireContext()).isShuffle

        shuffleMode.isEnabled = !IntroPrefs(requireContext()).hasIntroSlidesShown

        shuffleMode.setOnCheckedChangeListener { _, isChecked ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            if (isChecked) {
                if (!PreferenceUtil.checkPreferences("SHUFFLE_MODE")) {
                    MusicPlayerRemote.setShuffleMode(1)
                }
            }else {
                if (!PreferenceUtil.checkPreferences("SHUFFLE_MODE")) {
                    MusicPlayerRemote.setShuffleMode(0)
                }
            }

            IntroPrefs(requireContext()).isShuffle = isChecked
            PreferenceUtil.shouldRecreate = true
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_indigo_A400))
    }
    companion object {
        fun newInstance(): ShuffleSlideFragment {
            return ShuffleSlideFragment(R.color.md_indigo_A400)
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }
}