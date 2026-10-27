package com.mgneg.fakecall.update

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import kotlinx.coroutines.launch
import java.io.File

/** Installs a downloaded APK through a PackageInstaller session. */
object Installer {

    fun install(context: Context, apk: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("fake-call-update", 0, apk.length()).use { out ->
                    input.copyTo(out)
                    session.fsync(out)
                }
            }
            val intent = Intent(context, InstallResultReceiver::class.java)
                .setAction(ACTION_INSTALL_RESULT)
            val pending = PendingIntent.getBroadcast(
                context, sessionId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            session.commit(pending.intentSender)
        }
        // The session owns its copy now; the cache file is dead
        // weight and is deleted on successful commit (addendum U10).
        apk.delete()
    }

    const val ACTION_INSTALL_RESULT = "com.mgneg.fakecall.action.INSTALL_RESULT"
}

/**
 * Reports every install outcome back into the update flow (addendum
 * U7): previously only the confirm prompt was handled and every
 * failure — signature mismatch included — vanished silently.
 */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = if (Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                }
                confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (confirm != null) context.startActivity(confirm)
                InstallResults.report(InstallOutcome.AwaitingConfirm)
            }
            PackageInstaller.STATUS_SUCCESS ->
                InstallResults.report(InstallOutcome.Succeeded)
            PackageInstaller.STATUS_FAILURE_ABORTED ->
                InstallResults.report(InstallOutcome.Aborted)
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.w("FakeCallUpdate", "Install failed: status=$status message=$message")
                InstallResults.report(InstallOutcome.Failed(message))
                notifyFailureIfInBackground(context)
            }
        }
    }

    /**
     * If the user is not looking at the app (the sheet is gone), the
     * failure must still reach them: post it on the updates channel
     * with a route to the release page.
     */
    private fun notifyFailureIfInBackground(context: Context) {
        val am = context.getSystemService(ActivityManager::class.java)
        val foreground = am?.runningAppProcesses?.any {
            it.processName == context.packageName &&
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        } == true
        if (foreground) return
        val app = context.applicationContext as FakeCallApp
        val pending = goAsync()
        app.appScope.launch {
            try {
                val releaseUrl = app.updateRepository.available.value?.releaseUrl
                val builder = NotificationCompat.Builder(context, FakeCallApp.CHANNEL_UPDATES)
                    .setSmallIcon(R.drawable.ic_tile_phone)
                    .setContentTitle(context.getString(R.string.update_failed_install))
                    .setAutoCancel(true)
                if (releaseUrl != null) {
                    val open = PendingIntent.getActivity(
                        context, 4202,
                        Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(0, context.getString(R.string.update_open_github), open)
                }
                runCatching {
                    context.getSystemService(NotificationManager::class.java)
                        .notify(4202, builder.build())
                }
            } finally {
                pending.finish()
            }
        }
    }
}
