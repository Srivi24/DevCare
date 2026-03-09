package com.devcare.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.devcare.app.MainActivity
import com.devcare.app.data.model.Reminder
import com.devcare.app.engine.AlarmReceiver

/**
 * Handles notification creation with dynamic sound support and action buttons.
 */
object NotificationHelper {

    const val CHANNEL_BREAK_START = "devcare_break_start_channel"
    const val CHANNEL_BREAK_END = "devcare_break_end_channel"
    private const val CHANNEL_PREFIX_REMINDER = "devcare_reminder_"

    private const val NOTIFICATION_ID_REMINDER = 1001
    private const val NOTIFICATION_ID_BREAK_START = 1002
    private const val NOTIFICATION_ID_BREAK_END = 1003

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val breakStartChannel = NotificationChannel(
            CHANNEL_BREAK_START,
            "Break Start",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Special break start notifications"
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }

        val breakEndChannel = NotificationChannel(
            CHANNEL_BREAK_END,
            "Break End",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Special break end notifications"
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }

        manager.createNotificationChannels(listOf(breakStartChannel, breakEndChannel))
    }

    private fun getOrCreateReminderChannel(context: Context, toneUriString: String): String {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channelId = if (toneUriString == "default") {
            "devcare_reminder_default"
        } else {
            "${CHANNEL_PREFIX_REMINDER}${toneUriString.hashCode()}"
        }

        if (manager.getNotificationChannel(channelId) == null) {
            val soundUri = if (toneUriString == "default") {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else {
                Uri.parse(toneUriString)
            }

            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Regular health reminders"
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            manager.createNotificationChannel(channel)
        }
        return channelId
    }

    fun showReminderNotification(
        context: Context,
        reminders: List<Reminder>,
        showActions: Boolean = false,
        snoozeEnabled: Boolean = false
    ) {
        if (reminders.isEmpty()) return

        val toneUriString = reminders.firstOrNull()?.tone ?: "default"
        val channelId = getOrCreateReminderChannel(context, toneUriString)

        val text = reminders.joinToString("\n") { "${it.emoji} ${it.name}" }
        val title = if (reminders.size == 1) {
            "${reminders[0].emoji} ${reminders[0].name}"
        } else {
            "DevCare Reminder"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(if (reminders.size > 1) text else "Time for a healthy break!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (reminders.size > 1) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }

        if (showActions) {
            // Dismiss Action
            val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_DISMISS
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context, 0, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Dismiss", dismissPendingIntent)

            // Snooze Action
            if (snoozeEnabled) {
                val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                    action = AlarmReceiver.ACTION_SNOOZE
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context, 0, snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(0, "Snooze (5m)", snoozePendingIntent)
            }
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_REMINDER, builder.build())
    }

    fun cancelReminderNotification(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(NOTIFICATION_ID_REMINDER)
    }

    fun showBreakStartNotification(context: Context, reminderNames: String) {
        showNotification(
            context = context,
            channelId = CHANNEL_BREAK_START,
            notificationId = NOTIFICATION_ID_BREAK_START,
            title = "⏸️ Special Break Started",
            text = "$reminderNames — Take a break now!",
            bigText = null
        )
    }

    fun showBreakEndNotification(context: Context, reminderNames: String) {
        showNotification(
            context = context,
            channelId = CHANNEL_BREAK_END,
            notificationId = NOTIFICATION_ID_BREAK_END,
            title = "▶️ Break Over",
            text = "$reminderNames — Time to get back to work!",
            bigText = null
        )
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String,
        bigText: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (bigText != null) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, builder.build())
    }
}
