package com.devcare.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devcare.app.data.DevCareRepository
import com.devcare.app.data.model.Reminder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val repository: DevCareRepository
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = repository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertReminder(reminder: Reminder) {
        viewModelScope.launch { repository.insertReminder(reminder) }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch { repository.updateReminder(reminder) }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(enabled = !reminder.enabled))
        }
    }

    class Factory(private val repository: DevCareRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RemindersViewModel(repository) as T
        }
    }
}
