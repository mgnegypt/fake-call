package com.mgneg.fakecall.telecom

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R

/** Full-screen-intent notification that launches the app's call screen. */
object IncomingCallNotifier {

    private const val NOTIFICATION_ID = 4101

    fun show(context: Context, callerName: String, callerNumber: String) {
        val fullScreen = PendingIntent.getActivity(
            context, 0,
            Intent(context, IncomingCallActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Answer must launch the activity directly: a broadcast receiver may
        // not start an activity from a notification action on Android 12+.
        val answer = PendingIntent.getActivity(
            context, 1,
            Intent(context, IncomingCallActivity::class.java)
                .putExtra(IncomingCallActivity.EXTRA_ANSWER, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val decline = actionIntent(context, CallActionReceiver.ACTION_DECLINE, 2)
        val notification = NotificationCompat.Builder(context, FakeCallApp.CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_tile_phone)
            .setContentTitle(callerName.ifBlank { context.getString(R.string.call_unknown_caller) })
            .setContentText(callerNumber)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(fullScreen, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, context.getString(R.string.call_answer), answer)
            .addAction(0, context.getString(R.string.call_decline), decline)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    fun dismiss(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    private const val UNAVAILABLE_NOTIFICATION_ID = 4102

    /**
     * Honest failure notice for strict Telecom mode: the user picked the
     * system dialer, the call could not be placed there, and nothing rang.
     * Tapping the notification (or its action) opens the system
     * call-account settings where the account can be enabled. This is NOT
     * a call notification — no full-screen intent, no answer/decline.
     */
    fun showTelecomUnavailable(context: Context) {
        val settingsIntent = PendingIntent.getActivity(
            context, 3,
            TelecomAccounts.accountSettingsIntent()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, FakeCallApp.CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_tile_phone)
            .setContentTitle(context.getString(R.string.telecom_unavailable_title))
            .setContentText(context.getString(R.string.telecom_unavailable_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.telecom_unavailable_text))
            )
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(settingsIntent)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.action_enable), settingsIntent)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(UNAVAILABLE_NOTIFICATION_ID, notification)
    }

    private fun actionIntent(context: Context, action: String, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, requestCode,
            Intent(context, CallActionReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val controller = (context.applicationContext as FakeCallApp).callController
        when (intent.action) {
            ACTION_DECLINE -> controller.declineFromAppScreen()
        }
    }

    companion object {
        const val ACTION_DECLINE = "com.mgneg.fakecall.action.DECLINE"
    }
}
