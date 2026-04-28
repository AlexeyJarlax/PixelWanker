package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

/** Павлов Алексей https://github.com/AlexeyJarlax */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepository
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.pavlovalexey.pavlovAlexeySandbox.R

class InstalledAppsViewModel(
    private val repository: InstalledAppsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredApps: StateFlow<List<InstalledApp>> = combine(_apps, _searchQuery) { apps, query ->
        val q = query.trim()
        if (q.isEmpty()) {
            apps
        } else {
            apps.filter { app ->
                app.appName.contains(q, ignoreCase = true) ||
                        app.packageName.contains(q, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        loadApps()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _apps.value = repository.getInstalledApps()
                _uiState.value = UiState.Success()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(R.string.error_load_app_list)
            }
        }
    }
}

class InstalledAppsViewModelFactory(
    private val repository: InstalledAppsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstalledAppsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InstalledAppsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
