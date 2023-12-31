package com.ttop.app.apex.ui.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import com.ttop.app.apex.*
import com.ttop.app.apex.ui.utils.GithubUtils
import com.ttop.app.apex.util.PreferenceUtil

class UpdateSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {
        val update: Preference? = findPreference("update_button")
        update?.setOnPreferenceClickListener {
            var url = "https://theterminatorofprogramming.github.io/projects.html"
            if (PreferenceUtil.updateSource == "github") {
                GithubUtils.checkForUpdateGithub(requireContext(), true)
            }else {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://$url"
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_update)
    }
}
