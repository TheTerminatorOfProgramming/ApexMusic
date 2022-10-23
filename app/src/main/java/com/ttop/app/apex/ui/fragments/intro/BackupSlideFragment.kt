package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.appintro.SlidePolicy
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast

class BackupSlideFragment : Fragment() {

    private var desc: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_backup_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val description: String = "On foldables and tablets the list of backups does not load. To restore a backup click the restore button to manually select the backup file."

        desc = view.findViewById(R.id.description) as TextView

        desc?.text = description

        view.setBackgroundColor(resources.getColor(R.color.md_indigo_A400))
    }
    companion object {
        fun newInstance(fragmentRingtoneIntro: Int): BackupSlideFragment {
            return BackupSlideFragment()
        }
    }
}