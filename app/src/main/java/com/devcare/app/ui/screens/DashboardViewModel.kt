package com.devcare.app.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devcare.app.data.DevCareRepository
import com.devcare.app.data.model.Settings
import com.devcare.app.data.model.Statistics
import com.devcare.app.engine.EngineState
import com.devcare.app.engine.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DashboardViewModel(
    private val repository: DevCareRepository,
    private val context: Context
) : ViewModel() {

    private val engineState = EngineState(context.applicationContext)

    val settings: StateFlow<Settings> = repository.getSettings()
        .map { it ?: Settings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    val todayStats: StateFlow<Statistics> = repository.getTodayStatistics()
        .map { it ?: Statistics(date = LocalDate.now().toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Statistics(date = LocalDate.now().toString()))

    private val _isActive = MutableStateFlow(engineState.isActive)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    val nextAlarmTime: StateFlow<String> = combine(
        settings,
        _isActive
    ) { currentSettings, active ->
        formatNextAlarm(currentSettings, active)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), formatNextAlarm(Settings(), engineState.isActive))

    fun toggleEngine() {
        val currentSettings = settings.value
        if (!_isActive.value && ReminderScheduler.isOutsideWorkingHours(currentSettings)) {
            // Do not allow starting outside working hours
            return
        }

        viewModelScope.launch {
            if (_isActive.value) {
                // STOP
                engineState.isActive = false
                ReminderScheduler.cancel(context.applicationContext)
                engineState.resetAllCycles()
                repository.incrementStopCount()
                _isActive.value = false
            } else {
                // START
                engineState.isActive = true
                repository.incrementStartCount()
                ReminderScheduler.scheduleNext(context.applicationContext)
                _isActive.value = true
            }
        }
    }

    fun toggleFocusMode() {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(focusModeEnabled = !current.focusModeEnabled))
        }
    }

    fun refreshState() {
        val currentSettings = settings.value
        if (ReminderScheduler.isOutsideWorkingHours(currentSettings) && engineState.isActive) {
            stopEngineSilently()
        }
        _isActive.value = engineState.isActive
    }

    private fun stopEngineSilently() {
        engineState.isActive = false
        ReminderScheduler.cancel(context.applicationContext)
        _isActive.value = false
    }

    private fun formatNextAlarm(currentSettings: Settings, active: Boolean): String {
        if (ReminderScheduler.isOutsideWorkingHours(currentSettings)) {
            return "Your work time is not started yet!"
        }

        val millis = engineState.nextAlarmTimeMillis
        if (millis == 0L || !active) return "—"

        val now = LocalDateTime.now()
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(millis), ZoneId.systemDefault()
        )

        // If the next reminder is on a future day, show "Tomorrow will continue"
        if (dateTime.toLocalDate().isAfter(now.toLocalDate())) {
            return "Tomorrow will continue"
        }

        return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    class Factory(
        private val repository: DevCareRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository, context) as T
        }
    }
}
