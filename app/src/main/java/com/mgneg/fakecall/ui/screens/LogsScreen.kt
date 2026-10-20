package com.mgneg.fakecall.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.telecom.CallAttemptDiagnostics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The recorded ground truth of the most recent trigger attempt (see
 * [CallAttemptDiagnostics]): the mode, what our enabled-probe saw,
 * whether Telecom was asked, what it answered (exception included), and
 * how the registrar itself described the accounts. Lives on its own
 * screen (reached from the end of Settings) so a user can read it — and
 * export it as a plain-text file to share with support/the community
 * when a device misbehaves.
 */
@Composable
fun LogsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.logs_title), style = MaterialTheme.typography.titleLarge)

        Button(
            onClick = {
                val stamp = java.text.SimpleDateFormat(
                    "yyyyMMdd-HHmmss", java.util.Locale.US
                ).format(java.util.Date())
                exportLauncher.launch("fakecall-logs-$stamp.txt")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Text(
                stringResource(R.string.logs_export),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if (attempt.timestamp == 0L) {
            Text(stringResource(R.string.diag_none), style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                attempt.toDisplayLines().forEach { line ->
                    Text(
                        line,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
