package com.ttop.app.apex.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import com.ttop.app.apex.*
import com.ttop.app.apex.ui.utils.GithubUtils
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil

class UpdateSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {
        val update: Preference? = findPreference("update_button")
        update?.setOnPreferenceClickListener {
            if (PreferenceUtil.updateSource == "github") {
                GithubUtils.checkForUpdateGithub(requireContext(), true)
            }else {
                if (PreferenceUtil.isPreviewChannel) {
                    GithubUtils.checkForUpdateWebsitePreview(requireContext(), false)
                }else {
                    GithubUtils.checkForUpdateWebsite(requireContext(), false)
                }
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_update)
    }
}
