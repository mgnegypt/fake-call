package com.mgneg.fakecall

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.mgneg.fakecall.data.db.AppDatabase
import com.mgneg.fakecall.data.repo.CallRepository
import com.mgneg.fakecall.data.settings.SettingsStore
import com.mgneg.fakecall.scheduling.CallScheduler
import com.mgneg.fakecall.telecom.CallController
import com.mgneg.fakecall.telecom.TelecomAccounts
import com.mgneg.fakecall.update.UpdateRepository
import com.mgneg.fakecall.update.UpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FakeCallApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: AppDatabase
        private set
    lateinit var settings: SettingsStore
        private set
    lateinit var scheduler: CallScheduler
        private set
    lateinit var callController: CallController
        private set
    lateinit var callRepository: CallRepository
        private set
    lateinit var updateRepository: UpdateRepository
        private set

    /** True once one-time initialization (db, settings read, account) finished. */
    @Volatile
    var initialized: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.create(this)
        settings = SettingsStore(this)
        scheduler = CallScheduler(this)
        callController = CallController(this)
        callRepository = CallRepository(this, database, settings, scheduler, callController)
        updateRepository = UpdateRepository(this, settings)
        createNotificationChannels()
        appScope.launch {
            val label = settings.accountLabel.first()
            TelecomAccounts.register(this@FakeCallApp, label)
            initialized = true
        }
        UpdateWorker.schedulePeriodic(this)
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_CALLS, getString(R.string.notif_channel_calls),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_UPDATES, getString(R.string.notif_channel_updates),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    companion object {
        const val CHANNEL_CALLS = "fake_calls"
        const val CHANNEL_UPDATES = "app_updates"

        lateinit var instance: FakeCallApp
            private set
    }
}
