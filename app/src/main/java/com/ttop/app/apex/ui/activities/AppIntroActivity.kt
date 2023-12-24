package com.ttop.app.apex.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ttop.app.apex.R
import com.ttop.app.apex.ui.fragments.intro.*
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.IntroPrefs
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appintro.AppIntro2
import com.ttop.app.appintro.AppIntroPageTransformerType
import com.ttop.app.appthemehelper.util.VersionUtils


class AppIntroActivity() : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //MAIN SLIDE
        addSlide(MainSlideFragment.newInstance())

        //LANGUAGE SLIDE
        addSlide(LanguageSlideFragment.newInstance())

        if (VersionUtils.hasT()) {
            //NOTIFICATION SLIDE
            addSlide(NotificationSlideFragment.newInstance())
        }
        //SD CARD SLIDE
        addSlide(StorageSlideFragment.newInstance())
        //BLUETOOTH SLIDE
        if (VersionUtils.hasS()) {
            addSlide(BluetoothSlideFragment.newInstance())
        }
        //BATTERY OPTIMIZATION SLIDE
        if (VersionUtils.hasS()) {
            addSlide(BatterySlideFragment.newInstance())
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
                slideNumber = 3,
                required = false)
        }
        //SD Storage Access
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                slideNumber = 4,
                required = true)
        }else {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                slideNumber = 4,
                required = true)
        }
        //Bluetooth
        if (VersionUtils.hasS()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                slideNumber = 5,
                required = true)
        }

        setTransformer(AppIntroPageTransformerType.Flow)
        isVibrate = true
        isWizardMode = true
        isSystemBackButtonLocked = true
        isButtonsEnabled = false
        setImmersiveMode()
        setProgressIndicator()
    }
    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceUtil.isInternetConnected = ApexUtil.isNetworkAvailable(applicationContext)

        if (!IntroPrefs(applicationContext).hasIntroSlidesShown) {
            PreferenceManager.setDefaultValues(this, R.xml.pref_ui, false)
            IntroPrefs(applicationContext).hasIntroSlidesShown = true
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }else {
            finish()
        }
    }
}