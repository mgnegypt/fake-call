package com.mgneg.fakecall.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.i18n.LanguageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mgneg.fakecall.telecom.TelecomAccounts
import com.mgneg.fakecall.util.Permissions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onShowUpdate: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()

    val language by app.settings.language.collectAsState(initial = "system")
    val dynamicColor by app.settings.dynamicColor.collectAsState(initial = false)
    val callMode by app.settings.callMode.collectAsState(initial = "telecom")
    val updateNotifications by app.settings.updateNotifications.collectAsState(initial = false)

    var accountLabel by remember { mutableStateOf("") }
    var defaultName by remember { mutableStateOf("") }
    var defaultNumber by remember { mutableStateOf("") }
    // One-shot load in an effect — launching from the composition body
    // (the previous pattern) fires side effects during composition.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        accountLabel = app.settings.accountLabel.first()
        defaultName = app.settings.defaultName.first()
        defaultNumber = app.settings.defaultNumber.first()
    }

    var checking by remember { mutableStateOf(false) }
    var checkMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        SectionTitle(R.string.settings_language)
        listOf(
            "system" to stringResource(R.string.language_system),
            "en" to stringResource(R.string.language_en),
            "ar" to stringResource(R.string.language_ar)
        ).forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = language == value) {
                        scope.launch { app.settings.setLanguage(value) }
                        LanguageManager.apply(context, value)
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = language == value, onClick = null)
                Text(label, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }

        SectionTitle(R.string.settings_appearance)
        SwitchRow(
            title = stringResource(R.string.settings_dynamic_color),
            desc = stringResource(R.string.settings_dynamic_color_desc),
            checked = dynamicColor
        ) { scope.launch { app.settings.setDynamicColor(it) } }

        SectionTitle(R.string.settings_call_engine)
        listOf(
            "telecom" to stringResource(R.string.call_mode_telecom),
            "app" to stringResource(R.string.call_mode_app)
        ).forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = callMode == value) {
                        scope.launch { app.settings.setCallMode(value) }
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = callMode == value, onClick = null)
                Text(label, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        Text(stringResource(R.string.call_mode_desc), style = MaterialTheme.typography.bodySmall)

        SectionTitle(R.string.settings_account_label)
        OutlinedTextField(
            value = accountLabel, onValueChange = { accountLabel = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Text(stringResource(R.string.settings_account_label_desc), style = MaterialTheme.typography.bodySmall)
        Button(onClick = {
            scope.launch {
                app.settings.setAccountLabel(accountLabel)
                TelecomAccounts.register(context, accountLabel)
            }
        }) { Text(stringResource(R.string.action_save)) }

        SectionTitle(R.string.settings_defaults)
        OutlinedTextField(
            value = defaultName,
            onValueChange = { defaultName = it; scope.launch { app.settings.setDefaultName(it) } },
            label = { Text(stringResource(R.string.settings_default_name)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = defaultNumber,
            onValueChange = { defaultNumber = it; scope.launch { app.settings.setDefaultNumber(it) } },
            label = { Text(stringResource(R.string.settings_default_number)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        PermissionsSection(app)

        SectionTitle(R.string.settings_updates)
        SwitchRow(
            title = stringResource(R.string.settings_update_notifications),
            desc = null,
            checked = updateNotifications
        ) { scope.launch { app.settings.setUpdateNotifications(it) } }
        Button(
            onClick = {
                checking = true
                scope.launch {
                    val info = app.updateRepository.checkNow(manual = true)
                    checking = false
                    if (info != null) {
                        onShowUpdate()
                    } else {
                        checkMessage = context.getString(R.string.settings_up_to_date, BuildConfig.VERSION_NAME)
                    }
                }
            },
            enabled = !checking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (checking) R.string.settings_checking else R.string.settings_check_updates))
        }
        checkMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

        SectionTitle(R.string.settings_automation)
        Text(stringResource(R.string.settings_automation_desc), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SectionTitle(resId: Int) {
    Text(
        stringResource(resId),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SwitchRow(title: String, desc: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (desc != null) Text(desc, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/**
 * Persistent permissions checklist (also mirrored as auto-hiding cards on
 * Home). Every row shows the LIVE state, re-checked on ON_RESUME, with the
 * fastest path to grant: runtime dialog where the platform allows one,
 * the exact system settings screen otherwise.
 */
@Composable
private fun PermissionsSection(app: FakeCallApp) {
    val context = LocalContext.current
    val activity = context as? Activity

    var accountEnabled by remember { mutableStateOf(false) }
    var phoneState by remember { mutableStateOf(false) }
    var exactOk by remember { mutableStateOf(true) }
    var notifOk by remember { mutableStateOf(true) }
    var fsOk by remember { mutableStateOf(true) }
    var batteryOk by remember { mutableStateOf(true) }
    var installOk by remember { mutableStateOf(true) }

    fun refresh() {
        accountEnabled = TelecomAccounts.isEnabled(context)
        phoneState = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        exactOk = Permissions.canScheduleExact(context)
        notifOk = Permissions.notificationsEnabled(context)
        fsOk = Permissions.canFullScreenIntent(context)
        batteryOk = Permissions.isIgnoringBatteryOptimizations(context)
        installOk = Permissions.canInstallUnknownApps(context)
    }
    androidx.compose.runtime.LaunchedEffect(Unit) { refresh() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refresh() }

    val phoneStateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    SectionTitle(R.string.settings_permissions)
    PermissionRow(stringResource(R.string.account_card_title), accountEnabled) {
        if (!phoneState) phoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        else activity?.startActivity(TelecomAccounts.accountSettingsIntent())
    }
    PermissionRow(stringResource(R.string.perm_phone_state), phoneState) {
        phoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)
    }
    PermissionRow(stringResource(R.string.exact_alarm_card_title), exactOk) {
        activity?.startActivity(Permissions.exactAlarmSettingsIntent(context))
    }
    PermissionRow(stringResource(R.string.notifications_card_title), notifOk) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            activity?.startActivity(Permissions.notificationSettingsIntent(context))
        }
    }
    PermissionRow(stringResource(R.string.fullscreen_card_title), fsOk) {
        activity?.startActivity(Permissions.fullScreenSettingsIntent(context))
    }
    PermissionRow(stringResource(R.string.perm_battery), batteryOk) {
        activity?.startActivity(Permissions.batteryOptimizationIntent(context))
    }
    PermissionRow(stringResource(R.string.perm_install_updates), installOk) {
        activity?.startActivity(Permissions.installPermissionIntent(context))
    }
}

@Composable
private fun PermissionRow(title: String, granted: Boolean, onGrant: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(if (granted) R.string.perm_granted else R.string.perm_missing),
                style = MaterialTheme.typography.bodySmall,
                color = if (granted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
        if (!granted) {
            OutlinedButton(onClick = onGrant) {
                Text(stringResource(R.string.action_enable))
            }
        }
    }
}
