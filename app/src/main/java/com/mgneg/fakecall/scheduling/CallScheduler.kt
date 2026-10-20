package com.mgneg.fakecall.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mgneg.fakecall.util.Permissions

/** Schedules the alarm that fires a simulated call at an exact time. */
class CallScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(historyId: Long, triggerAtMillis: Long) {
        val pi = pendingIntent(historyId)
        if (Permissions.canScheduleExact(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun cancel(historyId: Long) {
        alarmManager.cancel(pendingIntent(historyId))
    }

    private fun pendingIntent(historyId: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            historyId.toInt(),
            Intent(context, AlarmReceiver::class.java).putExtra(AlarmReceiver.EXTRA_HISTORY_ID, historyId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
