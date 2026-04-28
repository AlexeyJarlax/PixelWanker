package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

/** Павлов Алексей https://github.com/AlexeyJarlax */

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlovalexey.pavlovAlexeySandbox.model.AppDetails
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepository
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.savedstate.SavedStateRegistryOwner
import com.pavlovalexey.pavlovAlexeySandbox.R

class AppDetailViewModel(
    private val repository: InstalledAppsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _details = MutableStateFlow<AppDetails?>(null)
    val details: StateFlow<AppDetails?> = _details

    private val _screenState = MutableStateFlow(AppDetailScreenState())
    val screenState: StateFlow<AppDetailScreenState> = _screenState

    private val _effects = MutableSharedFlow<AppDetailEffect>()
    val effects: SharedFlow<AppDetailEffect> = _effects.asSharedFlow()

    init {load()}

    private fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _details.value = repository.getAppDetails(packageName)
                _uiState.value = UiState.Success()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(R.string.error_load_app_info)
            }
        }
    }

    fun onOpenAppClick() {
        viewModelScope.launch {
            _effects.emit(AppDetailEffect.OpenApp(packageName))
        }
    }

    fun onOpenWithGridClick(shouldShowFirstLaunchDialog: Boolean) {
        if (shouldShowFirstLaunchDialog) {
            _screenState.value = AppDetailScreenState(
                showFirstLaunchDialog = true,
                pendingGridPackage = packageName
            )
            return
        }

        viewModelScope.launch {
            _effects.emit(AppDetailEffect.OpenWithGrid(packageName))
        }
    }

    fun onFirstLaunchDialogDismiss() {
        _screenState.value = AppDetailScreenState()
    }

    fun onFirstLaunchDialogConfirm() {
        val pendingPackage = _screenState.value.pendingGridPackage ?: return
        _screenState.value = AppDetailScreenState()
        viewModelScope.launch {
            _effects.emit(AppDetailEffect.OpenWithGrid(pendingPackage))
        }
    }
}

data class AppDetailScreenState(
    val showFirstLaunchDialog: Boolean = false,
    val pendingGridPackage: String? = null
)

sealed class AppDetailEffect {
    data class OpenApp(val packageName: String) : AppDetailEffect()
    data class OpenWithGrid(val packageName: String) : AppDetailEffect()
}

class AppDetailViewModelFactory(
    private val repository: InstalledAppsRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: android.os.Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(AppDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppDetailViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
