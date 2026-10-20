package com.mgneg.fakecall.update

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.MainActivity
import com.mgneg.fakecall.R
import com.mgneg.fakecall.data.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Holds the "update available" state the drawer and sheets observe. */
class UpdateRepository(
    private val context: Context,
    private val settings: SettingsStore
) {
    private val checker = UpdateChecker(settings)
    private val app get() = context.applicationContext as FakeCallApp

    private val _available = MutableStateFlow<UpdateInfo?>(null)
    val available: StateFlow<UpdateInfo?> = _available.asStateFlow()

    init {
        app.appScope.launch {
            settings.updateCache.collect { cache ->
                _available.value = cacheToInfo(cache)
            }
        }
    }

    suspend fun checkNow(manual: Boolean): UpdateInfo? {
        val before = _available.value
        val info = checker.check(manual)
        _available.value = info
        if (info != null && info.version != before?.version &&
            settings.updateNotifications.first()
        ) {
            notify(info)
        }
        return info
    }

    private fun cacheToInfo(cache: SettingsStore.UpdateCache): UpdateInfo? {
        val v = cache.latestVersion?.let { Version.parse(it) } ?: return null
        val current = Version.parse(com.mgneg.fakecall.BuildConfig.VERSION_NAME) ?: return null
        if (v <= current) return null
        return UpdateInfo(v, cache.notes, cache.releaseUrl, cache.apkUrl, cache.shaUrl, cache.date)
    }

    private fun notify(info: UpdateInfo) {
        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, FakeCallApp.CHANNEL_UPDATES)
            .setSmallIcon(R.drawable.ic_tile_phone)
            .setContentTitle(context.getString(R.string.notif_update_title, info.version.toString()))
            .setContentText(context.getString(R.string.notif_update_text))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        runCatching {
            context.getSystemService(NotificationManager::class.java).notify(4201, notification)
        }
    }
}
