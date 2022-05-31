package com.ttop.app.apex

import android.content.Context
import android.content.ContextWrapper
import android.os.LocaleList
import com.ttop.app.appthemehelper.util.VersionUtils.hasNougatMR
import com.google.android.gms.common.annotation.KeepName
import java.util.*

class LanguageContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        @KeepName
        fun wrap(context: Context?, newLocale: Locale?): LanguageContextWrapper {
            if (context == null) return LanguageContextWrapper(context)
            val configuration = context.resources.configuration
            if (hasNougatMR()) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.setLocale(newLocale)
            }
            return LanguageContextWrapper(context.createConfigurationContext(configuration))
        }
    }
}