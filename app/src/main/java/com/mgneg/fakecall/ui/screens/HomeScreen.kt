package com.mgneg.fakecall.ui.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.data.db.Preset
import com.mgneg.fakecall.ui.components.AppBackground
import com.mgneg.fakecall.ui.components.GradientTile
import com.mgneg.fakecall.ui.components.HeroCard
import com.mgneg.fakecall.ui.components.HeroChip
import com.mgneg.fakecall.ui.components.IconBubble
import com.mgneg.fakecall.ui.components.LabeledField
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.SoftChip
import com.mgneg.fakecall.ui.components.burgundy
import com.mgneg.fakecall.ui.components.primaryGradient
import com.mgneg.fakecall.ui.theme.BadgeRed
import kotlinx.coroutines.cancel
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
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override fun onCleared() {
        // The scope is hand-rolled (not viewModelScope), so cancel it
        // explicitly or its collectors outlive the ViewModel.
        scope.cancel()
    }

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

/** Largest hour count whose h*3600 + 59*60 + 59 still fits an Int. */
private const val MAX_CUSTOM_HOURS = (Int.MAX_VALUE - 3599) / 3600

@Composable
fun HomeScreen(
    snackbar: SnackbarHostState,
    onOpenPermissions: () -> Unit,
    onCallNowReady: ((() -> Unit)?) -> Unit
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
    val b = burgundy()
    val presets by vm.presets.collectAsState()
    val ringtoneUri by app.settings.ringtoneUri.collectAsState(initial = null)

    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var delayIndex by remember { mutableIntStateOf(1) }
    var customDelay by remember { mutableStateOf("") }
    var audioName by remember { mutableStateOf<String?>(null) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

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

    // Contacts: the app reads the device's contacts with READ_CONTACTS
    // (declared in the manifest, requested here at use time) and shows
    // its own searchable list; picking an entry fills the same name
    // and number fields as manual typing.
    var showContacts by remember { mutableStateOf(false) }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showContacts = true
        } else {
            scope.launch { snackbar.showSnackbar(context.getString(R.string.contacts_permission_denied)) }
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

    // Resolving a ringtone title queries the media provider — keep it
    // off the main thread and out of composition.
    val ringtoneTitle by androidx.compose.runtime.produceState<String?>(
        initialValue = null, ringtoneUri
    ) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                RingtoneManager.getRingtone(
                    context,
                    ringtoneUri?.let { Uri.parse(it) }
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                )?.getTitle(context)
            }.getOrNull()
        }
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

    // The floating bar's FAB is the same "Call now" action: register the
    // exact handler (current form values included) while Home is shown.
    DisposableEffect(name, number, audioPath) {
        onCallNowReady { startCall(0) }
        onDispose { onCallNowReady(null) }
    }

    val permissionEntries = rememberPermissionEntries(app)
    val missingPermissions = permissionEntries.count { !it.granted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header: logo tile + name; warning button with the missing count.
        Row(verticalAlignment = Alignment.CenterVertically) {
            GradientTile(Icons.Filled.Call, contentDescription = null)
            Text(
                stringResource(R.string.app_name),
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 10.dp).weight(1f)
            )
            if (missingPermissions > 0) {
                Box {
                    SoftCard(
                        radius = 23,
                        modifier = Modifier.size(46.dp),
                        onClick = onOpenPermissions
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Filled.WarningAmber,
                                contentDescription = pluralStringResource(
                                    R.plurals.permissions_need_enabling,
                                    missingPermissions, missingPermissions
                                ),
                                tint = b.accentText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(BadgeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            missingPermissions.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Title block.
        Column {
            Text(
                stringResource(R.string.home_ready),
                fontSize = 14.sp,
                color = b.muted
            )
            Text(
                stringResource(R.string.home_schedule_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Hero: ring + selected delay + chips.
        HeroCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DelayRing(delaySec = effectiveDelay)
                    Column {
                        Text(
                            stringResource(R.string.home_rings_in),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            delayText(effectiveDelay, customSelected = delayIndex == -1),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            stringResource(R.string.home_rings_auto),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(DELAY_OPTIONS) { i, sec ->
                        HeroChip(
                            text = delayChipText(sec),
                            selected = delayIndex == i,
                            onClick = { delayIndex = i }
                        )
                    }
                    item {
                        HeroChip(
                            text = stringResource(R.string.delay_custom_chip),
                            selected = delayIndex == -1,
                            onClick = { showCustomDialog = true }
                        )
                    }
                }
            }
        }

        // Caller card.
        SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(b.tint),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = name.trim().firstOrNull()?.uppercase()
                    if (initial != null) {
                        Text(
                            initial,
                            style = MaterialTheme.typography.titleLarge,
                            color = b.accentText
                        )
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = b.accentText,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                LabeledField(
                    label = stringResource(R.string.caller_name_label),
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.caller_name_placeholder),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Number field with the contacts button beside it
            // (maintainer's layout: the button moved here from a wide
            // pill above the fields).
            // IntrinsicSize.Max pins the row to the field's own height:
            // a plain fillMaxHeight on the button stretched the whole
            // row to the card's full height and broke the layout.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(androidx.compose.foundation.layout.IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LabeledField(
                    label = stringResource(R.string.caller_number_label),
                    value = number,
                    onValueChange = { number = it },
                    placeholder = stringResource(R.string.caller_number_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    ltrValue = true,
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(b.tint)
                        .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.READ_CONTACTS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                showContacts = true
                            } else {
                                contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Contacts,
                        contentDescription = null,
                        tint = b.accentText,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        stringResource(R.string.contacts_pick),
                        color = b.accentText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Two tiles: ringtone / answer audio.
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            HomeTile(
                icon = Icons.Filled.MusicNote,
                title = stringResource(R.string.ringtone_label),
                value = ringtoneTitle ?: stringResource(R.string.ringtone_default),
                filled = false,
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
                },
                modifier = Modifier.weight(1f)
            )
            HomeTile(
                icon = Icons.Filled.Mic,
                title = stringResource(R.string.audio_label),
                value = audioName ?: stringResource(R.string.audio_none),
                filled = false,
                onClick = { audioPicker.launch(arrayOf("audio/*")) },
                modifier = Modifier.weight(1f),
                trailing = if (audioPath != null) {
                    {
                        IconButton(
                            onClick = { audioPath = null; audioName = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.audio_clear),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else null
            )
        }

        // Big CTA.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(primaryGradient())
                .clickable(role = androidx.compose.ui.semantics.Role.Button) { startCall(effectiveDelay) }
                .padding(start = 24.dp, end = 8.dp)
        ) {
            Text(
                stringResource(R.string.home_start_call),
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF8A1B3F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Secondary actions.
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SoftChip(
                text = stringResource(R.string.call_now_button),
                selected = false,
                onClick = { startCall(0) },
                height = 52,
                modifier = Modifier.weight(1f)
            )
            SoftChip(
                text = stringResource(R.string.preset_save_button),
                selected = false,
                onClick = { showPresetDialog = true },
                height = 52,
                modifier = Modifier.weight(1f)
            )
        }

        // Presets.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.presets_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(R.string.presets_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = b.muted
            )
        }
        if (presets.isEmpty()) {
            Text(
                stringResource(R.string.presets_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = b.muted
            )
        } else {
            presets.forEach { preset ->
                SoftCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconBubble(
                            Icons.Filled.Bolt,
                            contentDescription = null,
                            size = 46,
                            iconSize = 20
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(preset.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (preset.delaySec > 0) {
                                    stringResource(
                                        R.string.preset_after,
                                        delayText(preset.delaySec, customSelected = false)
                                    )
                                } else {
                                    stringResource(R.string.preset_instant)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = b.muted
                            )
                        }
                        IconButton(onClick = { vm.deletePreset(preset) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = b.muted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(primaryGradient())
                                .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                                    vm.trigger(
                                        preset.callerName, preset.callerNumber,
                                        preset.audioUri, preset.delaySec
                                    ) {}
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.call_now_button),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showContacts) {
        ContactsPickerDialog(
            onDismiss = { showContacts = false },
            onPick = { pickedName, pickedNumber ->
                name = pickedName
                number = pickedNumber
                showContacts = false
            }
        )
    }

    if (showCustomDialog) {
        CustomTimeDialog(
            initialSeconds = customDelay.toIntOrNull() ?: 0,
            onConfirm = { totalSeconds ->
                customDelay = totalSeconds.toString()
                delayIndex = -1
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
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
private fun delayChipText(sec: Int): String = when {
    sec == 0 -> stringResource(R.string.delay_now)
    sec < 60 -> pluralStringResource(R.plurals.delay_seconds, sec, sec)
    else -> pluralStringResource(R.plurals.delay_minutes, sec / 60, sec / 60)
}

@Composable
private fun delayText(sec: Int, customSelected: Boolean): String = when {
    customSelected -> formatClock(sec)
    sec == 0 -> stringResource(R.string.delay_now)
    sec < 60 -> pluralStringResource(R.plurals.delay_seconds, sec, sec)
    sec % 60 == 0 -> pluralStringResource(R.plurals.delay_minutes, sec / 60, sec / 60)
    else -> formatClock(sec)
}

/** LTR clock format used for custom delays: 00:02:30 / 02:30. */
private fun formatClock(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

/** Decorative progress ring with the delay's largest unit in the centre. */
@Composable
private fun DelayRing(delaySec: Int) {
    val (value, unitRes) = when {
        delaySec >= 3600 -> (delaySec / 3600).toString() to R.string.unit_hours
        delaySec >= 60 -> (delaySec / 60).toString() to R.string.unit_minutes
        else -> delaySec.toString() to R.string.unit_seconds
    }
    Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = Color.White.copy(alpha = 0.22f),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke)
            )
            // Decorative arc (design sample ≈ 35%); not tied to logic.
            drawArc(
                color = Color.White,
                startAngle = -90f, sweepAngle = 360f * 0.35f, useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(unitRes),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** One of the two Home tiles (ringtone / answer audio). */
@Composable
private fun HomeTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val b = burgundy()
    SoftCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (filled) primaryGradient() else androidx.compose.ui.graphics.Brush.linearGradient(listOf(b.tint, b.tint))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (filled) Color.White else b.accentText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (trailing != null) {
                    Spacer(Modifier.weight(1f))
                    trailing()
                }
            }
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = b.muted,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Custom delay, board 02 presentation: h/m/s steppers over the same
 * total-seconds value the inline field used to edit. The board's
 * "at a specific time" segment is omitted — the app has no absolute-time
 * scheduling (plan section 5).
 */
@Composable
private fun CustomTimeDialog(
    initialSeconds: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val b = burgundy()
    var hours by remember { mutableIntStateOf(initialSeconds / 3600) }
    var minutes by remember { mutableIntStateOf((initialSeconds % 3600) / 60) }
    var seconds by remember { mutableIntStateOf(initialSeconds % 60) }
    val total = hours * 3600 + minutes * 60 + seconds

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                com.mgneg.fakecall.ui.components.BackHeader(
                    title = stringResource(R.string.custom_time_title),
                    onBack = onDismiss,
                    backContentDescription = stringResource(R.string.action_back)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StepperCard(
                        label = stringResource(R.string.unit_hours),
                        value = hours,
                        // Cap only where the total would overflow the
                        // Int seconds the scheduler accepts — the old
                        // digits field allowed any positive Int delay.
                        onChange = { hours = it.coerceIn(0, MAX_CUSTOM_HOURS) },
                        modifier = Modifier.weight(1f)
                    )
                    StepperCard(
                        label = stringResource(R.string.unit_minutes),
                        value = minutes,
                        onChange = { minutes = it.coerceIn(0, 59) },
                        modifier = Modifier.weight(1f)
                    )
                    StepperCard(
                        label = stringResource(R.string.unit_seconds),
                        value = seconds,
                        onChange = { seconds = it.coerceIn(0, 59) },
                        modifier = Modifier.weight(1f)
                    )
                }
                com.mgneg.fakecall.ui.components.HeroCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            stringResource(R.string.custom_summary),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            formatClock(total),
                            color = Color.White,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    com.mgneg.fakecall.ui.components.GradientButton(
                        text = stringResource(R.string.action_confirm),
                        onClick = { onConfirm(total) },
                        modifier = Modifier.weight(1f),
                        height = 56,
                        textSize = 17
                    )
                    SoftChip(
                        text = stringResource(R.string.action_cancel),
                        selected = false,
                        onClick = onDismiss,
                        height = 56
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperCard(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val b = burgundy()
    SoftCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = b.muted)
            StepperButton(Icons.Filled.Add, stringResource(R.string.action_increase)) { onChange(value + 1) }
            Text(
                "%02d".format(value),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )
            StepperButton(Icons.Filled.Remove, stringResource(R.string.action_decrease)) { onChange(value - 1) }
        }
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val b = burgundy()
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(b.tint)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = b.accentText)
    }
}


private data class ContactEntry(val name: String, val number: String)

/**
 * In-app contacts list (READ_CONTACTS). Reads every phone entry from
 * the device's contacts, deduped and sorted, with a search box;
 * picking one fills the caller name/number fields on Home.
 */
@Composable
private fun ContactsPickerDialog(
    onDismiss: () -> Unit,
    onPick: (name: String, number: String) -> Unit
) {
    val b = burgundy()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val contacts by produceState<List<ContactEntry>?>(initialValue = null) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val list = mutableListOf<ContactEntry>()
                context.contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null,
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )?.use { cursor ->
                    val seen = mutableSetOf<String>()
                    while (cursor.moveToNext()) {
                        val contactName = cursor.getString(0)?.trim().orEmpty()
                        val contactNumber = cursor.getString(1)?.trim().orEmpty()
                        if (contactNumber.isEmpty()) continue
                        val key = contactName.lowercase() + "|" +
                            contactNumber.filter { it.isDigit() }
                        if (seen.add(key)) list.add(ContactEntry(contactName, contactNumber))
                    }
                }
                list
            }.getOrDefault(emptyList())
        }
    }
    val filtered = remember(contacts, query) {
        val all = contacts ?: emptyList()
        if (query.isBlank()) all
        else {
            val queryDigits = query.filter { c -> c.isDigit() }
            all.filter {
                it.name.contains(query, ignoreCase = true) ||
                    (queryDigits.isNotEmpty() &&
                        it.number.filter { c -> c.isDigit() }.contains(queryDigits))
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                com.mgneg.fakecall.ui.components.BackHeader(
                    title = stringResource(R.string.contacts_title),
                    onBack = onDismiss,
                    backContentDescription = stringResource(R.string.action_back)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(b.card)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = b.muted,
                        modifier = Modifier.size(20.dp)
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = b.text,
                            fontSize = 15.sp,
                            fontFamily = com.mgneg.fakecall.ui.theme.CairoFamily
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(
                                    stringResource(R.string.contacts_search),
                                    color = b.muted,
                                    fontSize = 15.sp
                                )
                            }
                            inner()
                        }
                    )
                }
                when {
                    contacts == null -> Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = b.accent)
                    }
                    filtered.isEmpty() -> Text(
                        stringResource(R.string.contacts_empty),
                        color = b.muted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtered) { entry ->
                            SoftCard(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                                onClick = { onPick(entry.name, entry.number) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(b.tint),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initial = entry.name.trim().firstOrNull()?.uppercase()
                                        if (initial != null) {
                                            Text(
                                                initial,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = b.accentText
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = b.accentText,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (entry.name.isNotEmpty()) {
                                            Text(
                                                entry.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            entry.number,
                                            color = b.muted,
                                            fontSize = 13.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
