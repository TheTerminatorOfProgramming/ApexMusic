package com.ttop.app.apex.extensions

import android.content.Context
import android.view.Menu
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.ttop.app.apex.R
import java.util.*

fun Context.setUpMediaRouteButton(menu: Menu) {
    CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.action_cast)
}

fun FragmentActivity.installLanguageAndRecreate(code: String) {
    var mySessionId = 0

    val manager = SplitInstallManagerFactory.create(this)
    val listener = object: SplitInstallStateUpdatedListener{
        override fun onStateUpdate(state: SplitInstallSessionState) {
            if (state.sessionId() == mySessionId) {
                recreate()
                manager.unregisterListener(this)
            }
        }
    }
    manager.registerListener(listener)

    if (code != "auto") {
        // Try to download language resources
        val request =
            SplitInstallRequest.newBuilder().addLanguage(Locale.forLanguageTag(code))
                .build()
        manager.startInstall(request)
            // Recreate the activity on download complete
            .addOnSuccessListener {
                mySessionId = it
            }
            .addOnFailureListener {
                showToast("Language download failed.")
            }
    } else {
        recreate()
    }
}

fun Context.installSplitCompat() {
    SplitCompat.install(this)
}