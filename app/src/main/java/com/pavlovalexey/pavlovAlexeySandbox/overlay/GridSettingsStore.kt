package com.pavlovalexey.pavlovAlexeySandbox.overlay

/** Павлов Алексей https://github.com/AlexeyJarlax */

import android.content.Context
import android.graphics.Color

data class GridUserSettings(
    val cellValue: Int,
    val unit: String,       // "px" | "dp"
    val baseColor: Int,     // Color.BLACK/WHITE/RED (без альфы)
    val extraColor: Int?,   // ✅ NEW: второй цвет (nullable)
)

object GridSettingsStore {
    private const val PREFS = "pixel_wanker_grid_prefs"
    private const val KEY_SAVED = "saved"
    private const val KEY_CELL = "cell"
    private const val KEY_UNIT = "unit"
    private const val KEY_COLOR = "color"
    private const val KEY_HAS_EXTRA_COLOR = "has_extra_color"
    private const val KEY_EXTRA_COLOR = "extra_color"

    fun save(context: Context, settings: GridUserSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SAVED, true)
            .putInt(KEY_CELL, settings.cellValue)
            .putString(KEY_UNIT, settings.unit)
            .putInt(KEY_COLOR, settings.baseColor)
            .putBoolean(KEY_HAS_EXTRA_COLOR, settings.extraColor != null)
            .apply {
                if (settings.extraColor != null) {
                    putInt(KEY_EXTRA_COLOR, settings.extraColor)
                } else {
                    remove(KEY_EXTRA_COLOR)
                }
            }
            .apply()
    }

    fun loadOrNull(context: Context): GridUserSettings? {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!sp.getBoolean(KEY_SAVED, false)) return null

        val cell = sp.getInt(KEY_CELL, 20)
        val unit = sp.getString(KEY_UNIT, "px") ?: "px"
        val color = sp.getInt(KEY_COLOR, Color.BLACK)

        val hasExtra = sp.getBoolean(KEY_HAS_EXTRA_COLOR, false)
        val extraColor = if (hasExtra) sp.getInt(KEY_EXTRA_COLOR, Color.YELLOW) else null

        return GridUserSettings(
            cellValue = cell,
            unit = unit,
            baseColor = color,
            extraColor = extraColor
        )
    }

    fun loadOrDefault(context: Context): GridUserSettings =
        loadOrNull(context) ?: GridUserSettings(
            cellValue = 20,
            unit = "px",
            baseColor = Color.BLACK,
            extraColor = null
        )
}
