package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import android.graphics.Color
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PixelWankerUiStateMapperTest {

    @Test
    fun `toGridSettings maps UI fields to domain model`() {
        val uiState = PixelWankerUiState(
            unit = "dp",
            selectedSize = 30,
            baseColor = Color.RED,
            extraColor = Color.BLUE,
            showRussianTipsBlock = true,
            showFirstLaunchDialog = true,
            showTelegramStarsDialog = false,
            isCookieVisible1 = false,
            isCookieVisible2 = true,
            isPieVisible1 = false,
            isPieVisible2 = true
        )

        val mapped = uiState.toGridSettings()

        assertEquals(30, mapped.cellValue)
        assertEquals("dp", mapped.unit)
        assertEquals(Color.RED, mapped.baseColor)
        assertEquals(Color.BLUE, mapped.extraColor)
    }

    @Test
    fun `fromSavedSettings maps saved values and resets transient flags`() {
        val saved = GridUserSettings(
            cellValue = 22,
            unit = "mm",
            baseColor = Color.BLACK,
            extraColor = null
        )

        val state = PixelWankerUiState.fromSavedSettings(saved, showRussianTipsBlock = false)

        assertEquals(22, state.selectedSize)
        assertEquals("mm", state.unit)
        assertEquals(Color.BLACK, state.baseColor)
        assertEquals(null, state.extraColor)
        assertFalse(state.showRussianTipsBlock)
        assertFalse(state.showFirstLaunchDialog)
        assertFalse(state.showTelegramStarsDialog)
        assertTrue(state.isCookieVisible1)
        assertTrue(state.isCookieVisible2)
        assertTrue(state.isPieVisible1)
        assertTrue(state.isPieVisible2)
    }
}
