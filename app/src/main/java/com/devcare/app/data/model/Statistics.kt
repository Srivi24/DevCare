package com.devcare.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class Statistics(
    @PrimaryKey
    val date: String,               // format: "yyyy-MM-dd"
    val reminderTriggers: Int = 0,
    val specialBreakTriggers: Int = 0,
    val startCount: Int = 0,
    val stopCount: Int = 0
)
