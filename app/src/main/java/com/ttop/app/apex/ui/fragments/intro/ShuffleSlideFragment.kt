package com.ttop.app.apex.ui.fragments.intro

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.github.appintro.SlidePolicy
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.AppWidgetBig
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetFullCircle
import com.ttop.app.apex.extensions.showToast
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

        view.setBackgroundColor(resources.getColor(R.color.md_divider_dark_theme))
    }
    companion object {
        fun newInstance(fragmentRingtoneIntro: Int): ShuffleSlideFragment {
            return ShuffleSlideFragment()
        }
    }
}