package com.devcare.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey
    val id: Int = 1,                  // singleton row
    val workingStartTime: String = "09:00",   // HH:mm
    val workingEndTime: String = "18:00",     // HH:mm
    val themeMode: String = "system",  // "light", "dark", "system"
    val focusModeEnabled: Boolean = false,
    val snoozeEnabled: Boolean = false,
    val actionButtonsEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false
)
