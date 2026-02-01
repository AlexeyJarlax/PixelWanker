package com.pavlovalexey.pavlovAlexeySandbox.overlay

/** Павлов Алексей https://github.com/AlexeyJarlax */

import android.content.Context
import android.graphics.Color

data class GridUserSettings(
    val cellValue: Int,
    val unit: String,     // "px" | "dp"
    val baseColor: Int,   // Color.BLACK/WHITE/RED (без альфы)
)

object GridSettingsStore {
    private const val PREFS = "pixel_wanker_grid_prefs"
    private const val KEY_SAVED = "saved"
    private const val KEY_CELL = "cell"
    private const val KEY_UNIT = "unit"
    private const val KEY_COLOR = "color"

    fun save(context: Context, settings: GridUserSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SAVED, true)
            .putInt(KEY_CELL, settings.cellValue)
            .putString(KEY_UNIT, settings.unit)
            .putInt(KEY_COLOR, settings.baseColor)
            .apply()
    }

    fun loadOrNull(context: Context): GridUserSettings? {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!sp.getBoolean(KEY_SAVED, false)) return null

        val cell = sp.getInt(KEY_CELL, 20)
        val unit = sp.getString(KEY_UNIT, "px") ?: "px"
        val color = sp.getInt(KEY_COLOR, Color.BLACK)

        return GridUserSettings(cellValue = cell, unit = unit, baseColor = color)
    }

    fun loadOrDefault(context: Context): GridUserSettings =
        loadOrNull(context) ?: GridUserSettings(cellValue = 20, unit = "px", baseColor = Color.BLACK)
}