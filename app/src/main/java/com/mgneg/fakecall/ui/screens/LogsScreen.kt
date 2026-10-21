package com.mgneg.fakecall.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.telecom.CallAttemptDiagnostics
import com.mgneg.fakecall.ui.components.BackHeader
import com.mgneg.fakecall.ui.components.GradientButton
import com.mgneg.fakecall.ui.components.KeyValueRow
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.SoftDivider
import com.mgneg.fakecall.ui.components.burgundy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The recorded ground truth of the most recent trigger attempt (see
 * [CallAttemptDiagnostics]), burgundy layout: a friendly key/value card
 * over the same recorded fields, the registrar snapshot, and the raw
 * mono log box. The export is byte-identical to before (same
 * [CallAttemptDiagnostics.toLogText] via CreateDocument).
 */
@Composable
fun LogsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()
    val b = burgundy()

    val attempt by app.settings.lastCallAttempt
        .collectAsState(initial = CallAttemptDiagnostics())
    val latestAttempt by rememberUpdatedState(attempt)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val text = latestAttempt.toLogText(
                appName = context.getString(R.string.app_name),
                noAttemptNote = context.getString(R.string.diag_none)
            )
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(text.toByteArray(Charsets.UTF_8))
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackHeader(
            title = stringResource(R.string.logs_title),
            onBack = onBack,
            backContentDescription = stringResource(R.string.action_back)
        )

        GradientButton(
            text = stringResource(R.string.logs_export),
            onClick = {
                val stamp = java.text.SimpleDateFormat(
                    "yyyyMMdd-HHmmss", java.util.Locale.US
                ).format(java.util.Date())
                exportLauncher.launch("fakecall-logs-$stamp.txt")
            },
            modifier = Modifier.fillMaxWidth(),
            height = 52,
            textSize = 16,
            icon = Icons.Outlined.Download
        )

        if (attempt.timestamp == 0L) {
            Text(stringResource(R.string.diag_none), style = MaterialTheme.typography.bodySmall)
        } else {
            Text(
                stringResource(R.string.logs_last_attempt),
                style = MaterialTheme.typography.titleMedium
            )
            SoftCard {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    KeyValueRow(
                        stringResource(R.string.logs_mode),
                        when (attempt.mode) {
                            "telecom" -> stringResource(R.string.call_mode_telecom)
                            "app" -> stringResource(R.string.call_mode_app)
                            else -> attempt.mode
                        }
                    )
                    SoftDivider()
                    KeyValueRow(
                        stringResource(R.string.logs_account),
                        stringResource(
                            if (attempt.accountEnabled) R.string.common_enabled
                            else R.string.common_disabled
                        )
                    )
                    SoftDivider()
                    KeyValueRow(
                        stringResource(R.string.logs_attempted),
                        stringResource(
                            if (attempt.placeAttempted) R.string.common_yes
                            else R.string.common_no
                        )
                    )
                    SoftDivider()
                    KeyValueRow(
                        stringResource(R.string.logs_result),
                        when {
                            !attempt.placeAttempted -> stringResource(R.string.common_no)
                            attempt.placeResult -> stringResource(R.string.logs_result_success)
                            else -> stringResource(R.string.logs_result_failed)
                        }
                    )
                    if (attempt.placeErrorClass != null) {
                        SoftDivider()
                        KeyValueRow(
                            stringResource(R.string.logs_error),
                            "${attempt.placeErrorClass}: ${attempt.placeErrorMessage.orEmpty()}"
                        )
                    }
                    SoftDivider()
                    KeyValueRow(
                        stringResource(R.string.logs_route),
                        when (attempt.route) {
                            "TELECOM" -> stringResource(R.string.call_mode_telecom)
                            "APP_SCREEN" -> stringResource(R.string.call_mode_app)
                            "TELECOM_UNAVAILABLE" -> stringResource(R.string.logs_route_none)
                            else -> attempt.route
                        }
                    )
                }
            }

            if (attempt.snapshot.isNotEmpty()) {
                Text(
                    stringResource(R.string.logs_accounts),
                    style = MaterialTheme.typography.titleMedium
                )
                SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        attempt.snapshot.forEach { line ->
                            Text(
                                line,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = b.muted
                            )
                        }
                    }
                }
            }

            Text(
                stringResource(R.string.logs_raw),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                attempt.toDisplayLines().joinToString("\n"),
                fontSize = 12.sp,
                lineHeight = (12 * 1.7).sp,
                fontFamily = FontFamily.Monospace,
                color = b.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(b.card)
                    .padding(14.dp)
            )
        }
    }
}
