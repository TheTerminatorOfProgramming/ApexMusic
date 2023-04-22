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
import com.ttop.app.apex.extensions.hideStatusBar
import com.ttop.app.apex.extensions.setDrawBehindSystemBars
import com.ttop.app.apex.ui.fragments.intro.*
import com.ttop.app.apex.util.IntroPrefs
import com.ttop.app.appthemehelper.util.VersionUtils


class AppIntroActivity: AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //MAIN SLIDE
        addSlide(MainSlideFragment.newInstance())
        if (VersionUtils.hasT()) {
            //NOTIFICATION SLIDE
            addSlide(NotificationSlideFragment.newInstance())
        }
        //SD CARD SLIDE
        addSlide(StorageSlideFragment.newInstance())
        if (VersionUtils.hasS()) {
            //BLUETOOTH SLIDE
            addSlide(BluetoothSlideFragment.newInstance())
        }
        if (VersionUtils.hasS()) {
            //BATTERY OPTIMIZATION SLIDE
            addSlide(BatterySlideFragment.newInstance())
        }
        if (VersionUtils.hasMarshmallow()) {
            //RINGTONE SLIDE
            addSlide(RingtoneSlideFragment.newInstance())
        }
        //BLUETOOTH AUTOPLAY SLIDE
        addSlide(BluetoothAutoPlaySlideFragment.newInstance())
        //SHUFFLE SLIDE
        addSlide(ShuffleSlideFragment.newInstance())

        // Here we ask for permissions

        //Notification
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                slideNumber = 2,
                required = false)
        }
        //SD Storage Access
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                slideNumber = 3,
                required = true)
        }else {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                slideNumber = 3,
                required = true)
        }
        //Bluetooth
        if (VersionUtils.hasS()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                slideNumber = 4,
                required = true)
        }


        isWizardMode = true
        isSystemBackButtonLocked = true
        setImmersiveMode()
        setProgressIndicator()
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceManager.setDefaultValues(this, R.xml.pref_ui, false)
        IntroPrefs(applicationContext).hasIntroShown = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}