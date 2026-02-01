package com.pavlovalexey.pavlovAlexeySandbox.utils

import android.content.Context

object FirstLaunchDialogPrefs {
    private const val PREFS = "pixel_wanker_first_launch_prefs"
    private const val KEY_DIALOG_SHOWN = "first_launch_dialog_shown"

    fun shouldShow(context: Context): Boolean =
        !context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DIALOG_SHOWN, false)

    fun markShown(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DIALOG_SHOWN, true)
            .apply()
    }
}
