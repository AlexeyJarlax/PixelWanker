package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import com.pavlovalexey.pavlovAlexeySandbox.utils.FirstLaunchDialogPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PixelWankerViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(PixelWankerUiState.fromSavedSettings(GridSettingsStore.loadOrDefault(appContext)))
    val uiState: StateFlow<PixelWankerUiState> = _uiState.asStateFlow()

    val sizes: List<Int> = listOf(4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 40, 60, 100, 200, 300, 400)

    fun selectUnit(unit: String) {
        _uiState.update { it.copy(unit = unit) }
        saveCurrentSettings()
    }

    fun selectSize(size: Int) {
        _uiState.update { it.copy(selectedSize = size) }
        saveCurrentSettings()
    }

    fun selectBaseColor(color: Int) {
        _uiState.update { it.copy(baseColor = color) }
        saveCurrentSettings()
    }

    fun selectExtraColor(color: Int?) {
        _uiState.update { it.copy(extraColor = color) }
        saveCurrentSettings()
    }

    fun hideCookie1() = _uiState.update { it.copy(isCookieVisible1 = false) }
    fun hideCookie2() = _uiState.update { it.copy(isCookieVisible2 = false) }
    fun hidePie1() = _uiState.update { it.copy(isPieVisible1 = false) }
    fun hidePie2() = _uiState.update { it.copy(isPieVisible2 = false) }

    fun showTelegramStarsDialog() = _uiState.update { it.copy(showTelegramStarsDialog = true) }
    fun hideTelegramStarsDialog() = _uiState.update { it.copy(showTelegramStarsDialog = false) }

    fun onStartGridClick(): StartGridDecision {
        saveCurrentSettings()
        return if (FirstLaunchDialogPrefs.shouldShow(appContext)) {
            _uiState.update { it.copy(showFirstLaunchDialog = true) }
            StartGridDecision.WaitForFirstLaunchDialog
        } else {
            StartGridDecision.StartOverlay
        }
    }

    fun dismissFirstLaunchDialog() {
        _uiState.update { it.copy(showFirstLaunchDialog = false) }
    }

    fun confirmFirstLaunchDialog(): StartGridDecision {
        FirstLaunchDialogPrefs.markShown(appContext)
        _uiState.update { it.copy(showFirstLaunchDialog = false) }
        return StartGridDecision.StartOverlay
    }

    fun currentSettings(): GridUserSettings = _uiState.value.toGridSettings()

    private fun saveCurrentSettings() {
        GridSettingsStore.save(
            context = appContext,
            settings = currentSettings()
        )
    }
}

enum class StartGridDecision {
    WaitForFirstLaunchDialog,
    StartOverlay,
}

data class PixelWankerUiState(
    val unit: String,
    val selectedSize: Int,
    val baseColor: Int,
    val extraColor: Int?,
    val showFirstLaunchDialog: Boolean,
    val showTelegramStarsDialog: Boolean,
    val isCookieVisible1: Boolean,
    val isCookieVisible2: Boolean,
    val isPieVisible1: Boolean,
    val isPieVisible2: Boolean,
) {
    fun toGridSettings(): GridUserSettings = GridUserSettings(
        cellValue = selectedSize,
        unit = unit,
        baseColor = baseColor,
        extraColor = extraColor
    )

    companion object {
        fun fromSavedSettings(saved: GridUserSettings): PixelWankerUiState = PixelWankerUiState(
            unit = saved.unit,
            selectedSize = saved.cellValue,
            baseColor = saved.baseColor,
            extraColor = saved.extraColor,
            showFirstLaunchDialog = false,
            showTelegramStarsDialog = false,
            isCookieVisible1 = true,
            isCookieVisible2 = true,
            isPieVisible1 = true,
            isPieVisible2 = true,
        )
    }
}
