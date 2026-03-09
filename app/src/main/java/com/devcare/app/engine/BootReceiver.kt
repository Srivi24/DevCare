package com.devcare.app.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Restores alarms after device reboot.
 * Checks if the engine was active before reboot and reschedules.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val engineState = EngineState(context)
        if (engineState.isActive) {
            // Engine was active before reboot — reschedule next alarm
            ReminderScheduler.scheduleNext(context)
        }
    }
}
