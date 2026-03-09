package com.devcare.app.data.dao

import androidx.room.*
import com.devcare.app.data.model.Statistics
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Dao
interface StatisticsDao {

    @Query("SELECT * FROM statistics WHERE date = :date")
    suspend fun getByDate(date: String): Statistics?

    @Query("SELECT * FROM statistics WHERE date = :date")
    fun getByDateFlow(date: String): Flow<Statistics?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(statistics: Statistics)

    @Query("UPDATE statistics SET reminderTriggers = reminderTriggers + 1 WHERE date = :date")
    suspend fun incrementReminderTriggers(date: String)

    @Query("UPDATE statistics SET specialBreakTriggers = specialBreakTriggers + 1 WHERE date = :date")
    suspend fun incrementSpecialBreakTriggers(date: String)

    @Query("UPDATE statistics SET startCount = startCount + 1 WHERE date = :date")
    suspend fun incrementStartCount(date: String)

    @Query("UPDATE statistics SET stopCount = stopCount + 1 WHERE date = :date")
    suspend fun incrementStopCount(date: String)
}

fun todayDateString(): String =
    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
