package com.ttop.app.apex.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ttop.app.apex.DESATURATED_COLOR
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.ui.fragments.intro.BatterySlideFragment
import com.ttop.app.apex.ui.fragments.intro.BluetoothAutoPlaySlideFragment
import com.ttop.app.apex.ui.fragments.intro.BluetoothSlideFragment
import com.ttop.app.apex.ui.fragments.intro.LanguageSlideFragment
import com.ttop.app.apex.ui.fragments.intro.MainSlideFragment
import com.ttop.app.apex.ui.fragments.intro.NotificationSlideFragment
import com.ttop.app.apex.ui.fragments.intro.StorageSlideFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.AppIntroUtil
import com.ttop.app.apex.util.IntroPrefs
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appintro.AppIntro2
import com.ttop.app.appintro.AppIntroPageTransformerType
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.VersionUtils


class AppIntroActivity : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //MAIN SLIDE
        addSlide(MainSlideFragment.newInstance())

        //LANGUAGE SLIDE
        addSlide(LanguageSlideFragment.newInstance())

        //NOTIFICATION SLIDE
        if (VersionUtils.hasT()) {
            addSlide(NotificationSlideFragment.newInstance())
        }

        //SD CARD SLIDE
        addSlide(StorageSlideFragment.newInstance())

        //BLUETOOTH SLIDE
        addSlide(BluetoothSlideFragment.newInstance())

        //BLUETOOTH AUTOPLAY SLIDE
        addSlide(BluetoothAutoPlaySlideFragment.newInstance())

        //BATTERY OPTIMIZATION SLIDE
        if (!ApexUtil.hasBatteryPermission()) {
            addSlide(BatterySlideFragment.newInstance())
        }

        // Here we ask for permissions

        //Notification
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                slideNumber = AppIntroUtil.notificationPermission(),
                required = true
            )
        }
        //SD Storage Access
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                slideNumber = AppIntroUtil.storagePermission(),
                required = true
            )
        } else {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                slideNumber = AppIntroUtil.storagePermission(),
                required = true
            )
        }
        //Bluetooth
        askForPermissions(
            permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
            slideNumber = AppIntroUtil.bluetoothPermission(),
            required = true
        )

        setTransformer(AppIntroPageTransformerType.Flow)
        isVibrate = true
        vibrateDuration = 100L
        isWizardMode = true
        isSystemBackButtonLocked = true
        isSkipButtonEnabled = false
        isColorTransitionsEnabled = true
        setImmersiveMode()
        setDotIndicator()
        setSelectedIndicatorColor(applicationContext.accentColor())
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceUtil.isInternetConnected = ApexUtil.isNetworkAvailable(applicationContext)


        if (ApexUtil.isFoldable(applicationContext)) {
            PreferenceManager.setDefaultValues(this, R.xml.pref_general_foldable, false)
            PreferenceManager.setDefaultValues(this, R.xml.pref_now_playing_screen_foldable, false)
        }else {
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)
            PreferenceManager.setDefaultValues(this, R.xml.pref_now_playing_screen, false)
        }
        PreferenceManager.setDefaultValues(this, R.xml.pref_advanced, false)
        PreferenceManager.setDefaultValues(this, R.xml.pref_audio, false)
        PreferenceManager.setDefaultValues(this, R.xml.pref_images, false)
        PreferenceManager.setDefaultValues(this, R.xml.pref_labs, false)
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false)
        PreferenceManager.setDefaultValues(this, R.xml.pref_ui, false)

        ThemeStore.prefs(applicationContext).edit {
            putBoolean(DESATURATED_COLOR, true)
        }

        IntroPrefs(applicationContext).hasIntroSlidesShown = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}