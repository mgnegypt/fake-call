package com.mgneg.fakecall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.data.db.CallStatus
import com.mgneg.fakecall.ui.components.IconBubble
import com.mgneg.fakecall.ui.components.PillKind
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.StatusPill
import com.mgneg.fakecall.ui.components.burgundy
import com.mgneg.fakecall.util.rememberLtr
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * History, burgundy layout. Same data, same clear-all flow. The design
 * board's filter chips are intentionally NOT built: the app has no
 * history filtering, and the plan allows chips only where filtering
 * exists.
 */
@Composable
fun HistoryScreen(onScheduleCall: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()
    val b = burgundy()
    val entries by app.callRepository.history.collectAsState(initial = emptyList())
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 110.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineMedium)
            if (entries.isNotEmpty()) {
                TextButton(onClick = { confirmClear = true }) {
                    Text(stringResource(R.string.history_clear), color = b.accentText)
                }
            }
        }
        if (entries.isEmpty()) {
            // Empty board (design 07): big tint bubble + text + CTA home.
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(b.tint),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Call,
                        contentDescription = null,
                        tint = b.accentText,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Text(
                    stringResource(R.string.history_empty),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.5.dp, b.divider, RoundedCornerShape(22.dp))
                        .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onScheduleCall)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.history_schedule_cta),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 14.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val (icon, label, kind) = when (entry.status) {
                        CallStatus.ANSWERED ->
                            Triple(Icons.Filled.Call, R.string.history_answered, PillKind.Tint)
                        CallStatus.COMPLETED ->
                            Triple(Icons.Filled.Check, R.string.history_completed, PillKind.Muted)
                        CallStatus.DECLINED ->
                            Triple(Icons.Filled.Close, R.string.history_declined, PillKind.Danger)
                        CallStatus.FAILED ->
                            Triple(Icons.Filled.CallMissed, R.string.history_failed, PillKind.Danger)
                        else ->
                            Triple(Icons.Filled.Schedule, R.string.history_scheduled, PillKind.Muted)
                    }
                    SoftCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconBubble(icon, contentDescription = null, size = 46, iconSize = 22)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.callerName.ifBlank {
                                        stringResource(R.string.call_unknown_caller)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    rememberLtr(entry.callerNumber),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = b.muted
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    DateFormat.getDateTimeInstance(
                                        DateFormat.MEDIUM, DateFormat.SHORT
                                    ).format(Date(entry.triggerAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = b.muted
                                )
                                StatusPill(
                                    stringResource(label),
                                    kind,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                if (entry.durationSec > 0) {
                                    Text(
                                        stringResource(R.string.duration_seconds, entry.durationSec),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = b.muted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            text = { Text(stringResource(R.string.history_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    scope.launch { app.callRepository.clearHistory() }
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
