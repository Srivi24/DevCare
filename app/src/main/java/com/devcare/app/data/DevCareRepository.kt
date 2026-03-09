package com.devcare.app.data

import com.devcare.app.data.dao.ReminderDao
import com.devcare.app.data.dao.SettingsDao
import com.devcare.app.data.dao.StatisticsDao
import com.devcare.app.data.dao.todayDateString
import com.devcare.app.data.model.Reminder
import com.devcare.app.data.model.Settings
import com.devcare.app.data.model.Statistics
import kotlinx.coroutines.flow.Flow

class DevCareRepository(
    private val reminderDao: ReminderDao,
    private val statisticsDao: StatisticsDao,
    private val settingsDao: SettingsDao
) {
    // ── Reminders ──
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()
    fun getEnabledReminders(): Flow<List<Reminder>> = reminderDao.getEnabledReminders()
    suspend fun getEnabledRemindersList(): List<Reminder> = reminderDao.getEnabledRemindersList()
    suspend fun getReminderById(id: Long): Reminder? = reminderDao.getReminderById(id)
    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insert(reminder)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)
    suspend fun deleteReminderById(id: Long) = reminderDao.deleteById(id)

    // ── Statistics ──
    fun getTodayStatistics(): Flow<Statistics?> = statisticsDao.getByDateFlow(todayDateString())

    suspend fun ensureTodayExists() {
        val today = todayDateString()
        if (statisticsDao.getByDate(today) == null) {
            statisticsDao.upsert(Statistics(date = today))
        }
    }

    suspend fun incrementReminderTriggers() {
        ensureTodayExists()
        statisticsDao.incrementReminderTriggers(todayDateString())
    }

    suspend fun incrementSpecialBreakTriggers() {
        ensureTodayExists()
        statisticsDao.incrementSpecialBreakTriggers(todayDateString())
    }

    suspend fun incrementStartCount() {
        ensureTodayExists()
        statisticsDao.incrementStartCount(todayDateString())
    }

    suspend fun incrementStopCount() {
        ensureTodayExists()
        statisticsDao.incrementStopCount(todayDateString())
    }

    // ── Settings ──
    fun getSettings(): Flow<Settings?> = settingsDao.getSettings()
    suspend fun getSettingsOnce(): Settings = settingsDao.getSettingsOnce() ?: Settings()
    suspend fun updateSettings(settings: Settings) = settingsDao.upsert(settings)
}
