package com.devcare.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val intervalMinutes: Int,
    val specialCycle: Int = 0,        // 0 = no special cycle, N = every N cycles
    val specialBreakDurationMinutes: Int = 0,
    val tone: String = "default",     // "default" uses system notification sound
    val enabled: Boolean = true
)
