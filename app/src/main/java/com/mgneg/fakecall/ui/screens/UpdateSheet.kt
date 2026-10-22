package com.mgneg.fakecall.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.R
import com.mgneg.fakecall.i18n.LanguageManager
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.mgneg.fakecall.ui.components.FormattedNotes
import com.mgneg.fakecall.ui.components.GradientButton
import com.mgneg.fakecall.ui.components.HeroCard
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.SoftChip
import com.mgneg.fakecall.ui.components.burgundy
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

/**
 * Update sheet, restyled to the design's Update board but still a
 * bottom sheet (per the plan's deliverable decision). All updater
 * states, the SHA-256 verification and the install-permission gate are
 * the exact pre-redesign logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSheet(info: UpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val b = burgundy()
    var state by remember { mutableStateOf<DlState>(DlState.Idle) }
    val lang = LanguageManager.effectiveLanguage(context)
    val notes = remember(info) { ReleaseParser.notesFor(info.notesBody, lang) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = b.card2,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.update_title),
                style = MaterialTheme.typography.headlineMedium
            )

            HeroCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.update_available_label),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            rememberLtr("v" + info.version.toString()),
                            color = Color.White,
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            stringResource(R.string.update_current, BuildConfig.VERSION_NAME),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                    Text(
                        rememberLtr(info.date),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    when (val s = state) {
                        is DlState.Downloading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(s.pct / 100f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.White)
                                )
                            }
                            Text(
                                stringResource(R.string.update_downloading, s.pct),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        is DlState.Verifying -> Text(
                            stringResource(R.string.update_verifying),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        is DlState.Failed -> Text(
                            stringResource(R.string.update_failed),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        else -> Unit
                    }
                }
            }

            if (notes.isNotBlank()) {
                SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    FormattedNotes(
                        notes,
                        modifier = Modifier
                            .heightIn(max = 230.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
            if (!Permissions.canInstallUnknownApps(context)) {
                Text(
                    stringResource(R.string.update_install_permission),
                    style = MaterialTheme.typography.bodySmall,
                    color = b.muted
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GradientButton(
                    text = stringResource(R.string.update_download_install),
                    onClick = {
                        if (!Permissions.canInstallUnknownApps(context)) {
                            context.startActivity(Permissions.installPermissionIntent(context))
                            return@GradientButton
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
                    modifier = Modifier.weight(1f),
                    height = 56,
                    textSize = 17,
                    enabled = state is DlState.Idle || state is DlState.Failed
                )
                SoftChip(
                    text = stringResource(R.string.update_open_github),
                    selected = false,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(info.releaseUrl))
                        )
                    },
                    height = 56
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_dismiss), color = b.accentText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(version: String, notesBody: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val b = burgundy()
    val lang = LanguageManager.effectiveLanguage(context)
    val notes = remember(notesBody) { ReleaseParser.notesFor(notesBody, lang) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = b.card2,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.whats_new_title, version),
                style = MaterialTheme.typography.headlineMedium
            )
            if (notes.isNotBlank()) {
                SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    FormattedNotes(
                        notes,
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
            GradientButton(
                text = stringResource(R.string.action_dismiss),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                height = 52
            )
        }
    }
}
