package com.devcare.app.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.devcare.app.data.AppDatabase
import com.devcare.app.data.DevCareRepository
import com.devcare.app.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager.
 *
 * Flow:
 *  1. Determine if this is a normal reminder, break-end, or SNOOZE alarm
 *  2. Show appropriate notification
 *  3. Update statistics
 *  4. Handle special break cycles (schedule break-end if needed)
 *  5. Schedule the NEXT alarm
 *  6. Exit immediately (no long-running work)
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_IS_BREAK_END = "is_break_end"
        const val EXTRA_IS_SNOOZE = "is_snooze"
        const val EXTRA_REMINDER_IDS = "reminder_ids"
        const val EXTRA_REMINDER_NAMES = "reminder_names"
        
        const val ACTION_SNOOZE = "com.devcare.app.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.devcare.app.ACTION_DISMISS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val engineState = EngineState(context)
        if (!engineState.isActive) return

        when (intent.action) {
            ACTION_SNOOZE -> {
                handleSnoozeAction(context)
                return
            }
            ACTION_DISMISS -> {
                NotificationHelper.cancelReminderNotification(context)
                return
            }
        }

        val isBreakEnd = intent.getBooleanExtra(EXTRA_IS_BREAK_END, false)
        val isSnooze = intent.getBooleanExtra(EXTRA_IS_SNOOZE, false)

        if (isBreakEnd) {
            handleBreakEnd(context, engineState)
        } else if (isSnooze) {
            handleSnoozeTrigger(context, intent)
        } else {
            handleReminderTrigger(context, intent, engineState)
        }
    }

    private fun handleReminderTrigger(context: Context, intent: Intent, engineState: EngineState) {
        val reminderIds = intent.getLongArrayExtra(EXTRA_REMINDER_IDS) ?: longArrayOf()
        val db = AppDatabase.getInstance(context)
        val repository = DevCareRepository(
            db.reminderDao(), db.statisticsDao(), db.settingsDao()
        )

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repository.getSettingsOnce()
                val reminders = reminderIds.map { repository.getReminderById(it) }.filterNotNull()

                if (reminders.isEmpty()) {
                    ReminderScheduler.scheduleNext(context)
                    return@launch
                }

                // Check for special break triggers
                var hasSpecialBreak = false
                var maxBreakDuration = 0
                val specialBreakNames = mutableListOf<String>()

                for (reminder in reminders) {
                    if (reminder.specialCycle > 0) {
                        val cycle = engineState.incrementCycle(reminder.id)
                        if (cycle % reminder.specialCycle == 0) {
                            hasSpecialBreak = true
                            if (reminder.specialBreakDurationMinutes > maxBreakDuration) {
                                maxBreakDuration = reminder.specialBreakDurationMinutes
                            }
                            specialBreakNames.add("${reminder.emoji} ${reminder.name}")
                        }
                    }
                }

                if (hasSpecialBreak && maxBreakDuration > 0) {
                    val names = specialBreakNames.joinToString(", ")
                    NotificationHelper.showBreakStartNotification(context, names)
                    repository.incrementSpecialBreakTriggers()

                    val breakEndMillis = System.currentTimeMillis() + (maxBreakDuration * 60 * 1000L)
                    engineState.hasPendingBreakEnd = true
                    engineState.pendingBreakEndTimeMillis = breakEndMillis
                    engineState.pendingBreakReminderNames = names
                } else {
                    NotificationHelper.showReminderNotification(
                        context = context,
                        reminders = reminders,
                        showActions = settings?.actionButtonsEnabled ?: false,
                        snoozeEnabled = settings?.snoozeEnabled ?: false
                    )
                }

                repository.incrementReminderTriggers()
                ReminderScheduler.scheduleNext(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleBreakEnd(context: Context, engineState: EngineState) {
        val names = engineState.pendingBreakReminderNames
        NotificationHelper.showBreakEndNotification(context, names)
        engineState.clearPendingBreakEnd()
        ReminderScheduler.scheduleNext(context)
    }

    private fun handleSnoozeAction(context: Context) {
        NotificationHelper.cancelReminderNotification(context)
        // Snooze for 5 minutes
        val snoozeMillis = System.currentTimeMillis() + (5 * 60 * 1000L)
        ReminderScheduler.scheduleSnooze(context, snoozeMillis)
    }

    private fun handleSnoozeTrigger(context: Context, intent: Intent) {
        val reminderIds = intent.getLongArrayExtra(EXTRA_REMINDER_IDS) ?: longArrayOf()
        
        val db = AppDatabase.getInstance(context)
        val repository = DevCareRepository(
            db.reminderDao(), db.statisticsDao(), db.settingsDao()
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repository.getSettingsOnce()
                val reminders = reminderIds.map { repository.getReminderById(it) }.filterNotNull()
                
                if (reminders.isNotEmpty()) {
                    NotificationHelper.showReminderNotification(
                        context = context,
                        reminders = reminders,
                        showActions = settings?.actionButtonsEnabled ?: false,
                        snoozeEnabled = settings?.snoozeEnabled ?: false
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
