package com.pavlovalexey.pavlovAlexeySandbox.utils

import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrefsLogicTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun clearPrefs() {
        context.getSharedPreferences("pixel_wanker_first_launch_prefs", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("pixel_wanker_language_prefs", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("pixel_wanker_grid_prefs", android.content.Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun `first launch dialog is shown once`() {
        assertTrue(FirstLaunchDialogPrefs.shouldShow(context))

        FirstLaunchDialogPrefs.markShown(context)

        assertFalse(FirstLaunchDialogPrefs.shouldShow(context))
    }

    @Test
    fun `language prefs saves and reads language code`() {
        LanguagePrefs.setSelectedLanguage(context, "ru")

        assertEquals("ru", LanguagePrefs.getSelectedLanguage(context))
    }

    @Test
    fun `grid settings store saves and restores optional extra color`() {
        val settings = GridUserSettings(
            cellValue = 16,
            unit = "dp",
            baseColor = Color.GREEN,
            extraColor = Color.CYAN
        )

        GridSettingsStore.save(context, settings)

        assertEquals(settings, GridSettingsStore.loadOrNull(context))

        GridSettingsStore.save(
            context,
            settings.copy(extraColor = null)
        )

        assertEquals(null, GridSettingsStore.loadOrNull(context)?.extraColor)
    }
}
