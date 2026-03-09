package com.devcare.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devcare.app.data.DevCareRepository
import com.devcare.app.data.model.Statistics
import kotlinx.coroutines.flow.*

class StatisticsViewModel(
    private val repository: DevCareRepository
) : ViewModel() {

    val todayStats: StateFlow<Statistics> = repository.getTodayStatistics()
        .map { it ?: Statistics(date = "—") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Statistics(date = "—"))

    class Factory(private val repository: DevCareRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatisticsViewModel(repository) as T
        }
    }
}
