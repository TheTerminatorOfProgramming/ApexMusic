package com.ttop.app.apex.ui.activities

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appintro.AppIntro2
import com.ttop.app.appthemehelper.util.VersionUtils


class AppIntroActivityAbout : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //MAIN SLIDE
        addSlide(MainSlideFragment.newInstance())

        if (VersionUtils.hasT()) {
            //LANGUAGE SLIDE
            addSlide(LanguageSlideFragment.newInstance())
        }

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
                required = true)
        }
        //SD Storage Access
        if (VersionUtils.hasT()) {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                slideNumber = AppIntroUtil.storagePermission(),
                required = true)
        }else {
            askForPermissions(
                permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                slideNumber = AppIntroUtil.storagePermission(),
                required = false)
        }
        //Bluetooth
        askForPermissions(
            permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
            slideNumber = AppIntroUtil.bluetoothPermission(),
            required = true)

        setTransformer(AppIntroUtil.transformerType())
        isVibrate = true
        vibrateDuration = 100L
        isWizardMode = true
        isSystemBackButtonLocked = true
        isSkipButtonEnabled = true
        isColorTransitionsEnabled = true
        setImmersiveMode()
        setDotIndicator()
        setSelectedIndicatorColor(applicationContext.accentColor())
    }
    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceUtil.isInternetConnected = ApexUtil.isNetworkAvailable(applicationContext)

        finish()
    }
}