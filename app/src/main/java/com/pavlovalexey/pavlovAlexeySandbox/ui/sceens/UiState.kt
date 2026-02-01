package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens

import androidx.annotation.StringRes

sealed class UiState {
    object Loading : UiState()
    data class Success(val message: String? = null) : UiState()
    data class Error(@StringRes val messageResId: Int) : UiState()
}
