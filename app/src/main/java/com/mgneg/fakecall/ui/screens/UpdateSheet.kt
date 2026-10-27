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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
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
import com.mgneg.fakecall.update.ReleaseParser
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.update.UpdateFailReason
import com.mgneg.fakecall.update.UpdateInfo
import com.mgneg.fakecall.update.UpdateState
import com.mgneg.fakecall.util.Permissions
import com.mgneg.fakecall.util.rememberLtr

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
    val app = context.applicationContext as FakeCallApp
    val b = burgundy()
    // The flow lives in the controller now (addendum U1): this sheet
    // only observes it, so closing the sheet never kills a download.
    val controller = app.updateRepository.controller
    val state by controller.state.collectAsState()
    val busy = state is UpdateState.Downloading || state is UpdateState.CheckingChecksum ||
        state is UpdateState.Verifying || state is UpdateState.Installing
    // Fully expanded only, and while an update is busy the sheet cannot
    // be swiped/backed away — Cancel is the way out (addendum U2).
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !busy }
    )
    val lang = LanguageManager.effectiveLanguage(context)
    val notes = remember(info) { ReleaseParser.notesFor(info.notesBody, lang) }

    ModalBottomSheet(
        onDismissRequest = { if (!busy) onDismiss() },
        sheetState = sheetState,
        containerColor = b.card2,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
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
                    // Fixed-height status slot: the card is the same
                    // height in every state, so the sheet never jumps
                    // when the progress UI appears or leaves (U2).
                    Box(modifier = Modifier.heightIn(min = 44.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (val s = state) {
                        is UpdateState.Downloading -> {
                            if (s.pct != null) {
                                val animated by animateFloatAsState(
                                    targetValue = s.pct / 100f,
                                    animationSpec = tween(200),
                                    label = "downloadProgress"
                                )
                                LinearProgressIndicator(
                                    progress = { animated },
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.22f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    stringResource(R.string.update_downloading, s.pct),
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            } else {
                                LinearProgressIndicator(
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.22f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    stringResource(R.string.update_downloading_unknown),
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        is UpdateState.CheckingChecksum -> {
                            LinearProgressIndicator(
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.22f),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                stringResource(R.string.update_checking_checksum),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        is UpdateState.Verifying, is UpdateState.Installing, is UpdateState.AwaitingConfirm -> {
                            LinearProgressIndicator(
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.22f),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                stringResource(
                                    if (s is UpdateState.Installing || s is UpdateState.AwaitingConfirm) {
                                        R.string.update_installing
                                    } else {
                                        R.string.update_verifying
                                    }
                                ),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        is UpdateState.Failed -> Text(
                            stringResource(
                                when (s.reason) {
                                    UpdateFailReason.NETWORK ->
                                        R.string.update_failed_network
                                    UpdateFailReason.NOT_ENOUGH_STORAGE ->
                                        R.string.update_failed_storage
                                    UpdateFailReason.CHECKSUM_UNAVAILABLE ->
                                        R.string.update_failed_checksum_unavailable
                                    UpdateFailReason.CHECKSUM_MISMATCH ->
                                        R.string.update_verify_failed
                                    UpdateFailReason.INSTALL_FAILED ->
                                        R.string.update_failed_install
                                }
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        else -> Unit
                    }
                        }
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
                        controller.start(info)
                    },
                    modifier = Modifier.weight(1f),
                    height = 56,
                    textSize = 17,
                    enabled = state is UpdateState.Idle || state is UpdateState.Failed
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
            if (busy) {
                TextButton(
                    onClick = { controller.cancel(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_cancel), color = b.accentText)
                }
            } else {
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_dismiss), color = b.accentText)
                }
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
