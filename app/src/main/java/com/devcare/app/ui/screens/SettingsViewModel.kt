package com.devcare.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devcare.app.data.DevCareRepository
import com.devcare.app.data.model.Settings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: DevCareRepository,
    private val context: Context
) : ViewModel() {

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    val settings: StateFlow<Settings> = repository.getSettings()
        .map { 
            val s = it ?: Settings()
            _isLoaded.value = true
            s
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    fun updateWorkingStartTime(time: String) {
        viewModelScope.launch {
            repository.updateSettings(settings.value.copy(workingStartTime = time))
        }
    }

    fun updateWorkingEndTime(time: String) {
        viewModelScope.launch {
            repository.updateSettings(settings.value.copy(workingEndTime = time))
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateSettings(settings.value.copy(themeMode = mode))
        }
    }

    fun toggleSnooze() {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(snoozeEnabled = !current.snoozeEnabled))
        }
    }

    fun toggleActionButtons() {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(actionButtonsEnabled = !current.actionButtonsEnabled))
        }
    }

    fun openBatteryOptimization() {
        val intent = Intent(AndroidSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:com.devcare.app")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    class Factory(
        private val repository: DevCareRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, context) as T
        }
    }
}
