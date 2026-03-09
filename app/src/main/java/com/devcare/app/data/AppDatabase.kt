package com.devcare.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devcare.app.data.dao.ReminderDao
import com.devcare.app.data.dao.SettingsDao
import com.devcare.app.data.dao.StatisticsDao
import com.devcare.app.data.model.Reminder
import com.devcare.app.data.model.Settings
import com.devcare.app.data.model.Statistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Reminder::class, Statistics::class, Settings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "devcare.db"
            )
                .addCallback(PrepopulateCallback())
                .build()
        }
    }

    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Insert default reminders
                    val reminderDao = database.reminderDao()
                    reminderDao.insert(
                        Reminder(
                            name = "Drink Water",
                            emoji = "\uD83D\uDCA7",  // 💧
                            intervalMinutes = 30,
                            specialCycle = 2,
                            specialBreakDurationMinutes = 10,
                            tone = "default",
                            enabled = true
                        )
                    )
                    reminderDao.insert(
                        Reminder(
                            name = "Stretch",
                            emoji = "\uD83E\uDDD8",  // 🧘
                            intervalMinutes = 60,
                            specialCycle = 0,
                            specialBreakDurationMinutes = 0,
                            tone = "default",
                            enabled = true
                        )
                    )
                    reminderDao.insert(
                        Reminder(
                            name = "Eye Break",
                            emoji = "\uD83D\uDC40",  // 👀
                            intervalMinutes = 20,
                            specialCycle = 3,
                            specialBreakDurationMinutes = 5,
                            tone = "default",
                            enabled = true
                        )
                    )
                    reminderDao.insert(
                        Reminder(
                            name = "Pomodoro",
                            emoji = "\uD83C\uDF45",  // 🍅
                            intervalMinutes = 25,
                            specialCycle = 1,
                            specialBreakDurationMinutes = 5,
                            tone = "default",
                            enabled = false
                        )
                    )

                    // Insert default settings
                    database.settingsDao().upsert(Settings())
                }
            }
        }
    }
}
