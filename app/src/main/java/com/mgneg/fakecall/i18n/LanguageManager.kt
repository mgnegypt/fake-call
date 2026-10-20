package com.mgneg.fakecall.i18n

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Per-app language: LocaleManager on Android 13+, AppCompatDelegate below.
 * Values: "system", "en", "ar".
 */
object LanguageManager {

    fun apply(context: Context, language: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val lm = context.getSystemService(LocaleManager::class.java)
            lm.applicationLocales = if (language == "system") {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(language)
            }
        } else {
            AppCompatDelegate.setApplicationLocales(
                if (language == "system") LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(language)
            )
        }
    }

    /** The effective content language ("ar" or "en") for parsers and notes. */
    fun effectiveLanguage(context: Context): String {
        val locales = context.resources.configuration.locales
        val first = if (locales.isEmpty) "en" else locales.get(0).language
        return if (first.startsWith("ar")) "ar" else "en"
    }
}
