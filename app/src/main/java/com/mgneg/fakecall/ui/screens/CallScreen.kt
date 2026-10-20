package com.mgneg.fakecall.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.telecom.CallState
import com.mgneg.fakecall.util.rememberLtr
import kotlinx.coroutines.delay

private val AnswerGreen = Color(0xFF1FA855)
private val DeclineRed = Color(0xFFD93025)

/** The app's own incoming-call and in-call UI (fallback presentation path). */
@Composable
fun CallScreen(
    state: CallState,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onEnd: () -> Unit,
    onToggleMute: (Boolean) -> Unit,
    onToggleSpeaker: (Boolean) -> Unit,
    onFinished: () -> Unit
) {
    when (state) {
        is CallState.Idle -> LaunchedEffect(Unit) { onFinished() }
        is CallState.Ringing -> RingingContent(state, onAnswer, onDecline)
        is CallState.InCall -> InCallContent(state, onEnd, onToggleMute, onToggleSpeaker)
    }
}

@Composable
private fun CallerHeader(name: String, number: String, subtitle: String) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = name.ifBlank { stringResource(R.string.call_unknown_caller) },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = rememberLtr(number),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RingingContent(state: CallState.Ringing, onAnswer: () -> Unit, onDecline: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "ring")
    val pulse by transition.animateFloat(
        initialValue = 1f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        CallerHeader(state.callerName, state.callerNumber, stringResource(R.string.call_incoming))
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = onDecline,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = DeclineRed),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Filled.CallEnd, contentDescription = stringResource(R.string.call_decline),
                        tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.call_decline))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = onAnswer,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = AnswerGreen),
                    modifier = Modifier.size(72.dp).scale(pulse)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = stringResource(R.string.call_answer),
                        tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.call_answer))
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun InCallContent(
    state: CallState.InCall,
    onEnd: () -> Unit,
    onToggleMute: (Boolean) -> Unit,
    onToggleSpeaker: (Boolean) -> Unit
) {
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(state.startedAt) {
        while (true) {
            elapsed = (System.currentTimeMillis() - state.startedAt) / 1000
            delay(500)
        }
    }
    val timer = "%02d:%02d".format(elapsed / 60, elapsed % 60)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        CallerHeader(state.callerName, state.callerNumber, timer)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FilledIconButton(
                onClick = { onToggleMute(!state.muted) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (state.muted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    if (state.muted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = stringResource(R.string.call_mute)
                )
            }
            FilledIconButton(
                onClick = { onToggleSpeaker(!state.speaker) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (state.speaker) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = stringResource(R.string.call_speaker))
            }
        }
        Spacer(Modifier.height(28.dp))
        FilledIconButton(
            onClick = onEnd,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = DeclineRed),
            modifier = Modifier.size(72.dp)
        ) {
            Icon(Icons.Filled.CallEnd, contentDescription = stringResource(R.string.call_end),
                tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(48.dp))
    }
}
