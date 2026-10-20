package com.mgneg.fakecall.telecom

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FakeCallApp
        setContent {
            // Collected, never block the main thread on a DataStore read:
            // this activity must appear the instant a call fires.
            val dynamicColor by app.settings.dynamicColor.collectAsState(initial = false)
            FakeCallTheme(dynamicColor = dynamicColor) {
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
