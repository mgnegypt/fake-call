package com.mgneg.fakecall.telecom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.ui.screens.CallScreen
import com.mgneg.fakecall.ui.theme.FakeCallTheme

/** Lock-screen-capable host for the app's own incoming / in-call UI. */
class IncomingCallActivity : ComponentActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_ANSWER, false)) {
            (application as FakeCallApp).callController.answerCurrent()
        }
    }

    companion object {
        const val EXTRA_ANSWER = "com.mgneg.fakecall.extra.ANSWER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FakeCallApp
        if (intent?.getBooleanExtra(EXTRA_ANSWER, false) == true) {
            app.callController.answerCurrent()
        }
        setContent {
            // Collected, never block the main thread on a DataStore read:
            // this activity must appear the instant a call fires.
            // Same theme-mode resolution as MainActivity: a stored
            // light/dark choice wins over the system theme here too.
            val themeMode by app.settings.themeMode.collectAsState(initial = "system")
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            FakeCallTheme(darkTheme = darkTheme) {
                val state by app.callController.state.collectAsState()
                CallScreen(
                    state = state,
                    onAnswer = { app.callController.answerCurrent() },
                    onDecline = {
                        app.callController.declineCurrent()
                        finish()
                    },
                    onEnd = {
                        app.callController.endCurrent()
                        finish()
                    },
                    onToggleMute = { app.callController.setMuted(it) },
                    onToggleSpeaker = { app.callController.setSpeaker(it) },
                    onFinished = { finish() }
                )
            }
        }
    }
}
