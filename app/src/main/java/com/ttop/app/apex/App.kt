/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ttop.app.apex

import android.app.Application
import androidx.preference.PreferenceManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.ui.activities.ErrorActivity
import com.ttop.app.apex.ui.activities.MainActivity
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }
        // default theme
        if (!ThemeStore.isConfigured(this, 3)) {
            if (BuildConfig.DEBUG) {
                ThemeStore.editTheme(this)
                    .accentColorRes(R.color.default_debug_color)
                    .coloredNavigationBar(true)
                    .commit()
            } else {
                ThemeStore.editTheme(this)
                    .accentColorRes(R.color.default_color)
                    .coloredNavigationBar(true)
                    .commit()
            }
        }

        DynamicShortcutManager(this).initDynamicShortcuts()

        // setting Error activity
        CaocConfig.Builder.create().errorActivity(ErrorActivity::class.java)
            .restartActivity(MainActivity::class.java).apply()

        // Set Default values for now playing preferences
        // This will reduce startup time for now playing settings fragment as Preference listener of AbsSlidingMusicPanelActivity won't be called
        PreferenceManager.setDefaultValues(this, R.xml.pref_now_playing_screen, false)
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }
    }
}