package com.ttop.app.apex.ui.fragments.intro

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.github.appintro.SlidePolicy
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.appthemehelper.util.VersionUtils

class RingtoneSlideFragment : Fragment() {

    private lateinit var ringtone: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_ringtone_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ringtone = view.findViewById(R.id.permission_ringtone) as Button

        ringtone.setOnClickListener {
            if (!ApexUtil.hasAudioPermission() && VersionUtils.hasMarshmallow()) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = ("package:" + requireContext().packageName).toUri()
                startActivity(intent)
            }
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_red_400))
    }
    companion object {
        fun newInstance(): RingtoneSlideFragment {
            return RingtoneSlideFragment()
        }
    }
}