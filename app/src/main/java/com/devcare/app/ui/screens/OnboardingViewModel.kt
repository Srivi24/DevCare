package com.devcare.app.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devcare.app.data.DevCareRepository
import com.devcare.app.data.model.Reminder
import com.devcare.app.data.model.Settings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: DevCareRepository,
    private val context: Context
) : ViewModel() {

    val settings: StateFlow<Settings> = repository.getSettings()
        .map { it ?: Settings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    val reminders: StateFlow<List<Reminder>> = repository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateWorkingHours(start: String, end: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(workingStartTime = start, workingEndTime = end))
        }
    }

    fun updateTheme(mode: String) {
        viewModelScope.launch {
            repository.updateSettings(settings.value.copy(themeMode = mode))
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(enabled = !reminder.enabled))
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.updateSettings(settings.value.copy(onboardingCompleted = true))
        }
    }

    class Factory(
        private val repository: DevCareRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(repository, context) as T
        }
    }
}
