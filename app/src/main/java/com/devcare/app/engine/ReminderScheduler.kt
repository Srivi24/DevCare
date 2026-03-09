package com.devcare.app.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.devcare.app.data.AppDatabase
import com.devcare.app.data.model.Reminder
import com.devcare.app.data.model.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Core scheduling engine. Schedules exactly ONE alarm at a time using
 * AlarmManager.setExactAndAllowWhileIdle with RTC_WAKEUP.
 */
object ReminderScheduler {

    private const val REQUEST_CODE = 9001
    private const val SNOOZE_REQUEST_CODE = 9002
    private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Calculate and schedule the next alarm.
     */
    fun scheduleNext(context: Context) {
        val engineState = EngineState(context)
        if (!engineState.isActive) return

        if (engineState.hasPendingBreakEnd) {
            val breakEndTime = engineState.pendingBreakEndTimeMillis
            if (breakEndTime > System.currentTimeMillis()) {
                scheduleExactAlarm(context, breakEndTime, isBreakEnd = true)
                engineState.nextAlarmTimeMillis = breakEndTime
                return
            }
        }

        val db = AppDatabase.getInstance(context)
        val reminders: List<Reminder>
        val settings: Settings

        runBlocking(Dispatchers.IO) {
            reminders = db.reminderDao().getEnabledRemindersList()
            settings = db.settingsDao().getSettingsOnce() ?: Settings()
        }

        if (reminders.isEmpty()) return

        val now = LocalDateTime.now()
        val workStart = try { LocalTime.parse(settings.workingStartTime, TIME_FORMAT) } catch (e: Exception) { LocalTime.of(9,0) }
        val workEnd = try { LocalTime.parse(settings.workingEndTime, TIME_FORMAT) } catch (e: Exception) { LocalTime.of(18,0) }

        var earliestTime: LocalDateTime? = null
        val triggeringReminders = mutableListOf<ReminderTrigger>()

        for (reminder in reminders) {
            val nextTime = calculateNextTriggerTime(
                now = now,
                intervalMinutes = reminder.intervalMinutes,
                workStart = workStart,
                workEnd = workEnd
            )
            if (earliestTime == null || nextTime.isBefore(earliestTime)) {
                earliestTime = nextTime
                triggeringReminders.clear()
                triggeringReminders.add(ReminderTrigger(reminder, nextTime))
            } else if (nextTime.isEqual(earliestTime)) {
                triggeringReminders.add(ReminderTrigger(reminder, nextTime))
            }
        }

        if (earliestTime == null) return

        val millis = earliestTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val reminderIds = triggeringReminders.map { it.reminder.id }.toLongArray()
        val reminderNames = triggeringReminders.map { "${it.reminder.emoji} ${it.reminder.name}" }
            .joinToString("|")

        scheduleExactAlarm(
            context = context,
            timeMillis = millis,
            isBreakEnd = false,
            reminderIds = reminderIds,
            reminderNames = reminderNames
        )

        engineState.nextAlarmTimeMillis = millis
    }

    fun scheduleSnooze(context: Context, timeMillis: Long) {
        val db = AppDatabase.getInstance(context)
        val reminders: List<Reminder>
        runBlocking(Dispatchers.IO) {
            reminders = db.reminderDao().getEnabledRemindersList()
        }
        
        val reminderIds = reminders.map { it.id }.toLongArray()
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_IS_SNOOZE, true)
            putExtra(AlarmReceiver.EXTRA_REMINDER_IDS, reminderIds)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (canScheduleExactAlarms(alarmManager)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        }
    }

    fun isOutsideWorkingHours(settings: Settings): Boolean {
        val now = LocalTime.now()
        val workStart = try {
            LocalTime.parse(settings.workingStartTime, TIME_FORMAT)
        } catch (e: Exception) {
            LocalTime.of(9, 0)
        }
        val workEnd = try {
            LocalTime.parse(settings.workingEndTime, TIME_FORMAT)
        } catch (e: Exception) {
            LocalTime.of(18, 0)
        }
        return now.isBefore(workStart) || now.isAfter(workEnd)
    }

    private fun calculateNextTriggerTime(
        now: LocalDateTime,
        intervalMinutes: Int,
        workStart: LocalTime,
        workEnd: LocalTime
    ): LocalDateTime {
        val today = now.toLocalDate()

        if (now.toLocalTime().isBefore(workStart)) {
            return LocalDateTime.of(today, workStart).plusMinutes(intervalMinutes.toLong())
        }

        val nextTime = now.plusMinutes(intervalMinutes.toLong())

        if (!nextTime.toLocalTime().isAfter(workEnd)) {
            return nextTime
        }

        val tomorrow = today.plusDays(1)
        return LocalDateTime.of(tomorrow, workStart).plusMinutes(intervalMinutes.toLong())
    }

    private fun scheduleExactAlarm(
        context: Context,
        timeMillis: Long,
        isBreakEnd: Boolean,
        reminderIds: LongArray = longArrayOf(),
        reminderNames: String = ""
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_IS_BREAK_END, isBreakEnd)
            putExtra(AlarmReceiver.EXTRA_REMINDER_IDS, reminderIds)
            putExtra(AlarmReceiver.EXTRA_REMINDER_NAMES, reminderNames)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (canScheduleExactAlarms(alarmManager)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        }
    }

    private fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun cancel(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent)

        val engineState = EngineState(context)
        engineState.nextAlarmTimeMillis = 0L
        engineState.clearPendingBreakEnd()
    }

    private data class ReminderTrigger(
        val reminder: Reminder,
        val time: LocalDateTime
    )
}
