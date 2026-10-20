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
        val answer = actionIntent(context, CallActionReceiver.ACTION_ANSWER, 1)
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
            ACTION_ANSWER -> {
                controller.answerFromAppScreen()
                context.startActivity(
                    Intent(context, IncomingCallActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            }
            ACTION_DECLINE -> controller.declineFromAppScreen()
        }
    }

    companion object {
        const val ACTION_ANSWER = "com.mgneg.fakecall.action.ANSWER"
        const val ACTION_DECLINE = "com.mgneg.fakecall.action.DECLINE"
    }
}
