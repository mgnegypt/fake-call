package com.mgneg.fakecall.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.data.db.Preset
import com.mgneg.fakecall.telecom.TelecomAccounts
import com.mgneg.fakecall.update.UpdateInfo
import com.mgneg.fakecall.util.Permissions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(private val app: FakeCallApp) : ViewModel() {
    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default
    )

    val presets: StateFlow<List<Preset>> = app.callRepository.presets
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun trigger(name: String, number: String, audio: String?, delaySec: Int, done: () -> Unit) {
        scope.launch {
            app.callRepository.triggerCall(name, number, audio, delaySec)
            done()
        }
    }

    fun savePreset(name: String, callerName: String, callerNumber: String, audio: String?, delaySec: Int, result: (Boolean) -> Unit) {
        scope.launch {
            val existing = presets.value
            val slot = (1..5).firstOrNull { s -> existing.none { it.slot == s } }
            if (slot == null) {
                result(false)
                return@launch
            }
            result(
                app.callRepository.savePreset(
                    Preset(slot = slot, name = name, callerName = callerName,
                        callerNumber = callerNumber, audioUri = audio, delaySec = delaySec)
                )
            )
        }
    }

    fun deletePreset(preset: Preset) {
        scope.launch { app.callRepository.deletePreset(preset) }
    }
}

private val DELAY_OPTIONS = listOf(0, 10, 30, 60, 300, 900)

@Composable
fun HomeScreen(
    snackbar: SnackbarHostState,
    updateInfo: UpdateInfo?,
    onShowUpdate: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val vm: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(app) as T
        }
    })
    val scope = rememberCoroutineScope()
    val presets by vm.presets.collectAsState()
    val ringtoneUri by app.settings.ringtoneUri.collectAsState(initial = null)
    val vibrate by app.settings.vibrateOnRing.collectAsState(initial = true)

    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var delayIndex by remember { mutableIntStateOf(1) }
    var customDelay by remember { mutableStateOf("") }
    var audioName by remember { mutableStateOf<String?>(null) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var showPresetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        name = app.settings.defaultName.first()
        number = app.settings.defaultNumber.first()
    }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val dir = File(context.filesDir, "audio").apply { mkdirs() }
                val file = File(dir, "answer_${System.currentTimeMillis()}.audio")
                val ok = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { input.copyTo(it) }
                    }
                    true
                }.getOrDefault(false)
                if (ok) {
                    audioPath = file.absolutePath
                    audioName = uri.lastPathSegment?.substringAfterLast('/') ?: file.name
                }
            }
        }
    }

    val ringtonePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            scope.launch { app.settings.setRingtoneUri(uri?.toString()) }
        }
    }

    val ringtoneTitle = remember(ringtoneUri) {
        runCatching {
            RingtoneManager.getRingtone(
                context,
                ringtoneUri?.let { Uri.parse(it) }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            )?.getTitle(context)
        }.getOrNull()
    }

    val effectiveDelay = if (delayIndex in DELAY_OPTIONS.indices) DELAY_OPTIONS[delayIndex]
        else customDelay.toIntOrNull() ?: 0

    fun startCall(delaySec: Int) {
        vm.trigger(name, number, audioPath, delaySec) {
            if (delaySec > 0) scope.launch {
                snackbar.showSnackbar(
                    context.getString(
                        R.string.scheduled_confirmation,
                        context.resources.getQuantityString(R.plurals.delay_seconds, delaySec, delaySec)
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (updateInfo != null) {
            Card(
                onClick = onShowUpdate,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.SystemUpdate, contentDescription = null)
                    Text(
                        stringResource(R.string.nav_update_available, updateInfo.version.toString()),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
        }

        ReadinessCards()

        // Hero — mirrors the reference design's big primary card.
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null)
                }
                Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.hero_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Schedule form as one grouped card with rows.
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = name.trim().firstOrNull()?.uppercase()
                        if (initial != null) {
                            Text(initial, style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp).weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text(stringResource(R.string.caller_name_label)) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        OutlinedTextField(
                            value = number, onValueChange = { number = it },
                            label = { Text(stringResource(R.string.caller_number_label)) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                }

                HorizontalDivider()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.delay_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 10.dp))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DELAY_OPTIONS.forEachIndexed { i, sec ->
                        FilterChip(
                            selected = delayIndex == i,
                            onClick = { delayIndex = i },
                            label = {
                                Text(
                                    when {
                                        sec == 0 -> stringResource(R.string.delay_now)
                                        sec < 60 -> pluralStringResource(R.plurals.delay_seconds, sec, sec)
                                        else -> pluralStringResource(R.plurals.delay_minutes, sec / 60, sec / 60)
                                    }
                                )
                            }
                        )
                    }
                    FilterChip(
                        selected = delayIndex == -1,
                        onClick = { delayIndex = -1 },
                        label = { Text("…") }
                    )
                }
                if (delayIndex == -1) {
                    OutlinedTextField(
                        value = customDelay, onValueChange = { customDelay = it.filter(Char::isDigit) },
                        label = { Text(stringResource(R.string.delay_custom_label)) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                HorizontalDivider()

                FormRow(
                    icon = Icons.Filled.MusicNote,
                    title = stringResource(R.string.ringtone_label),
                    value = ringtoneTitle ?: stringResource(R.string.ringtone_default),
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(
                                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                ringtoneUri?.let { Uri.parse(it) }
                                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                            )
                        }
                        ringtonePicker.launch(intent)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Vibration, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.vibration_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 10.dp).weight(1f))
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { scope.launch { app.settings.setVibrateOnRing(it) } }
                    )
                }

                FormRow(
                    icon = Icons.Filled.Mic,
                    title = stringResource(R.string.audio_label),
                    value = audioName ?: stringResource(R.string.audio_none),
                    onClick = { audioPicker.launch(arrayOf("audio/*")) },
                    trailing = {
                        if (audioPath != null) {
                            IconButton(onClick = { audioPath = null; audioName = null }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.audio_clear))
                            }
                        }
                    }
                )
            }
        }

        // Call Now card
        Card(
            onClick = { startCall(0) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary)
                }
                Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                    Text(stringResource(R.string.call_now_button), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.callnow_subtitle), style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Button(
            onClick = { startCall(effectiveDelay) },
            modifier = Modifier.fillMaxWidth().height(58.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text(
                stringResource(R.string.home_start_call),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        OutlinedButton(onClick = { showPresetDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.preset_save_button))
        }

        Text(stringResource(R.string.presets_title), style = MaterialTheme.typography.titleMedium)
        if (presets.isEmpty()) {
            Text(stringResource(R.string.presets_empty), style = MaterialTheme.typography.bodyMedium)
        } else {
            presets.forEach { preset ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(preset.name, style = MaterialTheme.typography.titleSmall)
                            Text(preset.callerName, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = {
                            vm.trigger(preset.callerName, preset.callerNumber, preset.audioUri, preset.delaySec) {}
                        }) { Text(stringResource(R.string.call_now_button)) }
                        IconButton(onClick = { vm.deletePreset(preset) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }

    if (showPresetDialog) {
        var presetName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text(stringResource(R.string.preset_name_title)) },
            text = {
                OutlinedTextField(
                    value = presetName, onValueChange = { presetName = it },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPresetDialog = false
                    vm.savePreset(presetName.ifBlank { name }, name, number, audioPath, effectiveDelay) { ok ->
                        if (!ok) scope.launch {
                            snackbar.showSnackbar(context.getString(R.string.preset_limit_reached))
                        }
                    }
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun FormRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.padding(horizontal = 10.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(value, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

/**
 * Permission / readiness cards. Every check is re-run on ON_RESUME, so a
 * card disappears as soon as the permission is actually granted — the app
 * verifies the real state instead of assuming.
 */
@Composable
private fun ReadinessCards() {
    val context = LocalContext.current
    val activity = context as? Activity

    var accountEnabled by remember { mutableStateOf(false) }
    var exactOk by remember { mutableStateOf(true) }
    var notifOk by remember { mutableStateOf(true) }
    var fsOk by remember { mutableStateOf(true) }
    var phoneStateGranted by remember { mutableStateOf(false) }

    fun refresh() {
        accountEnabled = TelecomAccounts.isEnabled(context)
        exactOk = Permissions.canScheduleExact(context)
        notifOk = Permissions.notificationsEnabled(context)
        fsOk = Permissions.canFullScreenIntent(context)
        phoneStateGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) { refresh() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refresh() }

    val phoneStateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    if (!accountEnabled) {
        ReadinessCard(
            title = stringResource(R.string.account_card_title),
            text = stringResource(
                if (accountEnabled) R.string.account_enabled_text else R.string.account_disabled_text
            ),
            actionLabel = stringResource(R.string.action_enable),
            onAction = {
                if (!phoneStateGranted) {
                    phoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                } else {
                    activity?.startActivity(TelecomAccounts.accountSettingsIntent())
                }
            }
        )
    }
    if (!exactOk) {
        ReadinessCard(
            title = stringResource(R.string.exact_alarm_card_title),
            text = stringResource(R.string.exact_alarm_text),
            actionLabel = stringResource(R.string.action_enable),
            onAction = { activity?.startActivity(Permissions.exactAlarmSettingsIntent(context)) }
        )
    }
    if (!notifOk) {
        ReadinessCard(
            title = stringResource(R.string.notifications_card_title),
            text = stringResource(R.string.notifications_text),
            actionLabel = stringResource(R.string.action_enable),
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    activity?.startActivity(Permissions.notificationSettingsIntent(context))
                }
            }
        )
    }
    if (!fsOk) {
        ReadinessCard(
            title = stringResource(R.string.fullscreen_card_title),
            text = stringResource(R.string.fullscreen_text),
            actionLabel = stringResource(R.string.action_enable),
            onAction = { activity?.startActivity(Permissions.fullScreenSettingsIntent(context)) }
        )
    }
}

@Composable
private fun ReadinessCard(title: String, text: String, actionLabel: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(text, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
