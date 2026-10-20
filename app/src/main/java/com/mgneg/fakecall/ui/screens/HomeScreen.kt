package com.mgneg.fakecall.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.data.db.Preset
import com.mgneg.fakecall.telecom.TelecomAccounts
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
fun HomeScreen(snackbar: SnackbarHostState) {
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

    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var delayIndex by remember { mutableIntStateOf(0) }
    var customDelay by remember { mutableStateOf("") }
    var audioName by remember { mutableStateOf<String?>(null) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var showPresetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        name = app.settings.defaultName.first()
        number = app.settings.defaultNumber.first()
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
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

    val effectiveDelay = if (delayIndex in DELAY_OPTIONS.indices) DELAY_OPTIONS[delayIndex]
        else customDelay.toIntOrNull() ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReadinessCards()
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.titleLarge)
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
        Text(stringResource(R.string.delay_label), style = MaterialTheme.typography.titleSmall)
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
        Text(stringResource(R.string.audio_label), style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Audiotrack, contentDescription = null)
            Text(
                text = audioName ?: stringResource(R.string.audio_none),
                modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
            )
            OutlinedButton(onClick = { picker.launch(arrayOf("audio/*")) }) {
                Text(stringResource(if (audioPath == null) R.string.audio_pick else R.string.audio_change))
            }
            if (audioPath != null) {
                IconButton(onClick = { audioPath = null; audioName = null }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.audio_clear))
                }
            }
        }
        Button(
            onClick = {
                vm.trigger(name, number, audioPath, effectiveDelay) {
                    scope.launch {
                        snackbar.showSnackbar(
                            context.getString(
                                R.string.scheduled_confirmation,
                                context.resources.getQuantityString(
                                    R.plurals.delay_seconds, effectiveDelay, effectiveDelay
                                )
                            )
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (effectiveDelay == 0) R.string.call_now_button else R.string.schedule_button))
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
        Spacer(Modifier.height(8.dp))
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
private fun ReadinessCards() {
    val context = LocalContext.current
    val activity = context as? Activity
    // Re-read on composition; these change rarely and the screen recomposes on nav.
    val accountEnabled = remember { TelecomAccounts.isEnabled(context) }
    val exactOk = remember { Permissions.canScheduleExact(context) }
    val notifOk = remember { Permissions.notificationsEnabled(context) }
    val fsOk = remember { Permissions.canFullScreenIntent(context) }

    if (!accountEnabled) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.account_card_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.account_disabled_text), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = {
                    activity?.startActivity(TelecomAccounts.accountSettingsIntent())
                }) { Text(stringResource(R.string.action_enable)) }
            }
        }
    }
    if (!exactOk) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.exact_alarm_card_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.exact_alarm_text), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = {
                    activity?.startActivity(Permissions.exactAlarmSettingsIntent(context))
                }) { Text(stringResource(R.string.action_enable)) }
            }
        }
    }
    if (!notifOk) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.notifications_card_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.notifications_text), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = {
                    activity?.startActivity(Permissions.notificationSettingsIntent(context))
                }) { Text(stringResource(R.string.action_enable)) }
            }
        }
    }
    if (!fsOk) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.fullscreen_card_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.fullscreen_text), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = {
                    activity?.startActivity(Permissions.fullScreenSettingsIntent(context))
                }) { Text(stringResource(R.string.action_enable)) }
            }
        }
    }
}
