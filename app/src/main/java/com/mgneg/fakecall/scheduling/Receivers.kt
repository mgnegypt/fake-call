package com.mgneg.fakecall.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mgneg.fakecall.FakeCallApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_HISTORY_ID, -1L)
        if (id <= 0) return
        val app = context.applicationContext as FakeCallApp
        val pending = goAsync()
        app.appScope.launch {
            try {
                app.callRepository.fireNow(id)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HISTORY_ID = "history_id"
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Package replace matters as much as boot: AlarmManager alarms are
        // cleared when the app is updated, and this app updates itself.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val app = context.applicationContext as FakeCallApp
        val pending = goAsync()
        app.appScope.launch {
            try {
                val label = app.settings.accountLabel.first()
                com.mgneg.fakecall.telecom.TelecomAccounts.register(app, label)
                app.callRepository.reschedulePending()
            } finally {
                pending.finish()
            }
        }
    }
}
