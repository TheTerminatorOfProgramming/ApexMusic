package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.util.PreferenceUtil

class ShuffleSlideFragment : Fragment() {

    private lateinit var shuffleMode: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_shuffle_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shuffleMode = view.findViewById(R.id.shuffleMode) as SwitchCompat

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

            PreferenceUtil.shouldRecreate = true
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_indigo_A400))
    }
    companion object {
        fun newInstance(): ShuffleSlideFragment {
            return ShuffleSlideFragment()
        }
    }
}