package com.mgneg.fakecall.telecom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.ui.screens.CallScreen
import com.mgneg.fakecall.ui.theme.FakeCallTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Lock-screen-capable host for the app's own incoming / in-call UI. */
class IncomingCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FakeCallApp
        val dynamicColor = runBlocking { app.settings.dynamicColor.first() }
        setContent {
            FakeCallTheme(dynamicColor = dynamicColor) {
                val state by app.callController.state.collectAsState()
                CallScreen(
                    state = state,
                    onAnswer = { app.callController.answerFromAppScreen() },
                    onDecline = {
                        app.callController.declineFromAppScreen()
                        finish()
                    },
                    onEnd = {
                        app.callController.endFromAppScreen()
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
