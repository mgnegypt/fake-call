package com.mgneg.fakecall.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mgneg.fakecall.FakeCallApp
import kotlinx.coroutines.launch

/**
 * Automation API for Tasker / MacroDroid / ADB.
 *
 * Action: com.mgneg.fakecall.action.TRIGGER
 * Extras: caller_name (String), caller_number (String), delay (Int seconds).
 * Missing extras fall back to the saved defaults.
 */
class AutomationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER) return
        val app = context.applicationContext as FakeCallApp
        val name = intent.getStringExtra(EXTRA_CALLER_NAME)
        val number = intent.getStringExtra(EXTRA_CALLER_NUMBER)
        val delay = intent.getIntExtra(EXTRA_DELAY, 0)
        val pending = goAsync()
        app.appScope.launch {
            try {
                app.callRepository.triggerCall(name, number, null, delay)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER = "com.mgneg.fakecall.action.TRIGGER"
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_CALLER_NUMBER = "caller_number"
        const val EXTRA_DELAY = "delay"
    }
}
