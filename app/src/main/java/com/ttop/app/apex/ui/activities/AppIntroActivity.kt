package com.ttop.app.apex.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.appintro.AppIntro
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.ttop.app.apex.R
import com.ttop.app.apex.ui.fragments.intro.*
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.util.VersionUtils


class AppIntroActivity: AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //MAIN SLIDE
        addSlide(MainSlideFragment.newInstance(R.layout.fragment_main_intro))
        //SD CARD SLIDE
        addSlide(StorageSlideFragment.newInstance(R.layout.fragment_storage_intro))
        if (VersionUtils.hasS()) {
            //BLUETOOTH SLIDE
            addSlide(BluetoothSlideFragment.newInstance(R.layout.fragment_bluetooth_intro))
        }
        if (VersionUtils.hasT()) {
            //NOTIFICATION SLIDE
            addSlide(NotificationSlideFragment.newInstance(R.layout.fragment_notification_intro))
        }
        if (VersionUtils.hasS()) {
            //BATTERY OPTIMIZATION SLIDE
            addSlide(BatterySlideFragment.newInstance(R.layout.fragment_battery_intro))
        }
        //RINGTONE SLIDE
        addSlide(RingtoneSlideFragment.newInstance(R.layout.fragment_ringtone_intro))
        //BLUETOOTH AUTOPLAY SLIDE
        addSlide(BluetoothAutoPlaySlideFragment.newInstance(R.layout.fragment_bluetooth_autoplay_intro))
        //WHITELIST SLIDE
        addSlide(WhitelistSlideFragment.newInstance(R.layout.fragment_whitelist_intro))
        //BACKUP RESTORE SLIDE
        addSlide(BackupSlideFragment.newInstance(R.layout.fragment_backup_intro))
        //SHUFFLE SLIDE
        addSlide(ShuffleSlideFragment.newInstance(R.layout.fragment_shuffle_intro))
        //APP MODE SLIDE
        addSlide(AppModeSlideFragment.newInstance(R.layout.fragment_app_mode_intro))

        // Here we ask for permissions

        //SD Storage Access
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_MEDIA_AUDIO ),
                slideNumber = 2,
                required = true)
        }else {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                slideNumber = 2,
                required = true)
        }
        //Bluetooth
        if (VersionUtils.hasS()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                slideNumber = 3,
                required = true)
        }
        //Notification
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                slideNumber = 4,
                required = false)
        }


        isWizardMode = true
        isSystemBackButtonLocked = true
        setImmersiveMode()
        setStatusBarColorRes(R.color.md_black_1000)
        setProgressIndicator()
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceManager.setDefaultValues(this, R.xml.pref_ui, false)
        PreferenceUtil.hasIntroShown = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}