package com.devcare.app.engine

import android.content.Context
import android.content.SharedPreferences

/**
 * Lightweight SharedPreferences wrapper for persisting engine state
 * (active status, next alarm time, per-reminder cycle counters).
 * No background service — just key-value reads/writes.
 */
class EngineState(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("devcare_engine", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE = "is_active"
        private const val KEY_NEXT_ALARM = "next_alarm_millis"
        private const val KEY_CYCLE_PREFIX = "cycle_"
        private const val KEY_PENDING_BREAK_END = "pending_break_end"
        private const val KEY_PENDING_BREAK_END_TIME = "pending_break_end_time"
        private const val KEY_PENDING_BREAK_REMINDERS = "pending_break_reminders"
    }

    var isActive: Boolean
        get() = prefs.getBoolean(KEY_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_ACTIVE, value).apply()

    var nextAlarmTimeMillis: Long
        get() = prefs.getLong(KEY_NEXT_ALARM, 0L)
        set(value) = prefs.edit().putLong(KEY_NEXT_ALARM, value).apply()

    /** Get the current cycle count for a specific reminder. */
    fun getCycleCount(reminderId: Long): Int =
        prefs.getInt("$KEY_CYCLE_PREFIX$reminderId", 0)

    /** Increment and return the new cycle count for a reminder. */
    fun incrementCycle(reminderId: Long): Int {
        val newCount = getCycleCount(reminderId) + 1
        prefs.edit().putInt("$KEY_CYCLE_PREFIX$reminderId", newCount).apply()
        return newCount
    }

    /** Reset cycle count for a reminder. */
    fun resetCycle(reminderId: Long) {
        prefs.edit().remove("$KEY_CYCLE_PREFIX$reminderId").apply()
    }

    /** Reset all cycle counts. */
    fun resetAllCycles() {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(KEY_CYCLE_PREFIX) }.forEach { editor.remove(it) }
        editor.apply()
    }

    // ── Pending special break end tracking ──

    var hasPendingBreakEnd: Boolean
        get() = prefs.getBoolean(KEY_PENDING_BREAK_END, false)
        set(value) = prefs.edit().putBoolean(KEY_PENDING_BREAK_END, value).apply()

    var pendingBreakEndTimeMillis: Long
        get() = prefs.getLong(KEY_PENDING_BREAK_END_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_PENDING_BREAK_END_TIME, value).apply()

    /** Comma-separated reminder names for break end notification. */
    var pendingBreakReminderNames: String
        get() = prefs.getString(KEY_PENDING_BREAK_REMINDERS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PENDING_BREAK_REMINDERS, value).apply()

    fun clearPendingBreakEnd() {
        prefs.edit()
            .remove(KEY_PENDING_BREAK_END)
            .remove(KEY_PENDING_BREAK_END_TIME)
            .remove(KEY_PENDING_BREAK_REMINDERS)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
