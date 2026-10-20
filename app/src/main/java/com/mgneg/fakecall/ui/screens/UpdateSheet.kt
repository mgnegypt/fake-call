package com.mgneg.fakecall.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.i18n.LanguageManager
import com.mgneg.fakecall.update.Installer
import com.mgneg.fakecall.update.ReleaseParser
import com.mgneg.fakecall.update.Sha256
import com.mgneg.fakecall.update.UpdateDownloader
import com.mgneg.fakecall.update.UpdateInfo
import com.mgneg.fakecall.util.Permissions
import com.mgneg.fakecall.util.rememberLtr
import kotlinx.coroutines.launch

private sealed interface DlState {
    data object Idle : DlState
    data class Downloading(val pct: Int) : DlState
    data object Verifying : DlState
    data object Failed : DlState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSheet(info: UpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<DlState>(DlState.Idle) }
    val lang = LanguageManager.effectiveLanguage(context)
    val notes = remember(info) { ReleaseParser.notesFor(info.notesBody, lang) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(R.string.update_title), style = MaterialTheme.typography.titleLarge)
            Text(
                rememberLtr(stringResource(R.string.update_new_version, info.version.toString()) + " · " + info.date),
                style = MaterialTheme.typography.bodyMedium
            )
            if (notes.isNotBlank()) {
                Text(notes, style = MaterialTheme.typography.bodyMedium)
            }
            when (val s = state) {
                is DlState.Downloading ->
                    Text(stringResource(R.string.update_downloading, s.pct))
                is DlState.Verifying -> Text(stringResource(R.string.update_verifying))
                is DlState.Failed -> Text(
                    stringResource(R.string.update_failed),
                    color = MaterialTheme.colorScheme.error
                )
                else -> Unit
            }
            Button(
                onClick = {
                    if (!Permissions.canInstallUnknownApps(context)) {
                        context.startActivity(Permissions.installPermissionIntent(context))
                        return@Button
                    }
                    val downloader = UpdateDownloader(context)
                    scope.launch {
                        state = DlState.Downloading(0)
                        val file = downloader.downloadApk(info) { pct ->
                            state = DlState.Downloading(pct)
                        }
                        if (file == null) {
                            state = DlState.Failed
                            return@launch
                        }
                        val expected = downloader.fetchExpectedSha(info)
                        if (expected != null) {
                            state = DlState.Verifying
                            if (!Sha256.verify(file, expected)) {
                                state = DlState.Failed
                                return@launch
                            }
                        }
                        Installer.install(context, file)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state is DlState.Idle || state is DlState.Failed
            ) { Text(stringResource(R.string.update_download_install)) }
            if (!Permissions.canInstallUnknownApps(context)) {
                Text(stringResource(R.string.update_install_permission), style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.releaseUrl)))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.update_open_github)) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_dismiss))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(version: String, notesBody: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lang = LanguageManager.effectiveLanguage(context)
    val notes = remember(notesBody) { ReleaseParser.notesFor(notesBody, lang) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.whats_new_title, version),
                style = MaterialTheme.typography.titleLarge
            )
            if (notes.isNotBlank()) {
                Text(notes, style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_dismiss))
            }
        }
    }
}
