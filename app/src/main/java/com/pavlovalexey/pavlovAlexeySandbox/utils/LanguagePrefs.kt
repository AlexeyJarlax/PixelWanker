package com.pavlovalexey.pavlovAlexeySandbox.utils

import android.content.Context

object LanguagePrefs {
    private const val PREFS = "pixel_wanker_language_prefs"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"

    fun getSelectedLanguage(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_LANGUAGE, null)

    fun setSelectedLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_LANGUAGE, languageCode)
            .apply()
    }
}
