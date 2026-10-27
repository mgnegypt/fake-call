package com.mgneg.fakecall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.i18n.LanguageManager
import com.mgneg.fakecall.telecom.TelecomAccounts
import com.mgneg.fakecall.trigger.AutomationReceiver
import com.mgneg.fakecall.ui.components.AppSwitch
import com.mgneg.fakecall.ui.components.DropdownField
import com.mgneg.fakecall.ui.components.GradientButton
import com.mgneg.fakecall.ui.components.IconBubble
import com.mgneg.fakecall.ui.components.LabeledField
import com.mgneg.fakecall.ui.components.SegmentedControl
import com.mgneg.fakecall.ui.components.SettingRow
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.SoftDivider
import com.mgneg.fakecall.ui.components.burgundy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Settings, burgundy layout. Sections and order follow the design board;
 * every control is wired to the exact same setting/handler as before the
 * redesign (language, theme mode, call mode, account label,
 * defaults, permissions, updates, automation, logs).
 */
@Composable
fun SettingsScreen(onShowUpdate: () -> Unit, onOpenLogs: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()
    val b = burgundy()

    val language by app.settings.language.collectAsState(initial = "system")
    val themeMode by app.settings.themeMode.collectAsState(initial = "system")
    val callMode by app.settings.callMode.collectAsState(initial = "telecom")
    val updateNotifications by app.settings.updateNotifications.collectAsState(initial = false)

    var accountLabel by remember { mutableStateOf("") }
    var defaultName by remember { mutableStateOf("") }
    var defaultNumber by remember { mutableStateOf("") }
    // One-shot load in an effect — launching from the composition body
    // fires side effects during composition.
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
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

        // Language + appearance, one grouped card.
        SoftCard {
            Column {
                SettingRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.settings_language)
                ) {
                    val languageOptions = listOf(
                        "system" to stringResource(R.string.language_system),
                        "en" to stringResource(R.string.language_en),
                        "ar" to stringResource(R.string.language_ar)
                    )
                    DropdownField(
                        value = languageOptions.firstOrNull { it.first == language }?.second
                            ?: languageOptions[0].second,
                        options = languageOptions.map { it.second },
                        onSelect = { index ->
                            val value = languageOptions[index].first
                            scope.launch { app.settings.setLanguage(value) }
                            LanguageManager.apply(context, value)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                SoftDivider()
                SettingRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = stringResource(R.string.settings_dark_mode_desc)
                ) {
                    val themeOptions = listOf(
                        "system" to stringResource(R.string.theme_system),
                        "light" to stringResource(R.string.theme_light),
                        "dark" to stringResource(R.string.theme_dark)
                    )
                    DropdownField(
                        value = themeOptions.firstOrNull { it.first == themeMode }?.second
                            ?: themeOptions[0].second,
                        options = themeOptions.map { it.second },
                        onSelect = { index ->
                            scope.launch { app.settings.setThemeMode(themeOptions[index].first) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Call display mode.
        SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_call_engine), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.call_mode_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = b.muted
                )
                SegmentedControl(
                    options = listOf(
                        stringResource(R.string.call_mode_telecom),
                        stringResource(R.string.call_mode_app)
                    ),
                    selectedIndex = if (callMode == "app") 1 else 0,
                    onSelect = { index ->
                        scope.launch {
                            app.settings.setCallMode(if (index == 1) "app" else "telecom")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Call account label.
        SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_account_label), style = MaterialTheme.typography.titleMedium)
                LabeledField(
                    label = stringResource(R.string.field_name_label),
                    value = accountLabel,
                    onValueChange = { accountLabel = it },
                    placeholder = "Fake Call",
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.settings_account_label_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = b.muted
                )
                GradientButton(
                    text = stringResource(R.string.action_save),
                    onClick = {
                        scope.launch {
                            app.settings.setAccountLabel(accountLabel)
                            TelecomAccounts.register(context, accountLabel)
                        }
                    }
                )
            }
        }

        // Default caller.
        SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_defaults), style = MaterialTheme.typography.titleMedium)
                LabeledField(
                    label = stringResource(R.string.settings_default_name),
                    value = defaultName,
                    onValueChange = { v ->
                        defaultName = v
                        scope.launch { app.settings.setDefaultName(v) }
                    },
                    placeholder = stringResource(R.string.caller_name_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )
                LabeledField(
                    label = stringResource(R.string.settings_default_number),
                    value = defaultNumber,
                    onValueChange = { v ->
                        defaultNumber = v
                        scope.launch { app.settings.setDefaultNumber(v) }
                    },
                    placeholder = stringResource(R.string.caller_number_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Permissions checklist.
        SoftCard {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Text(
                    stringResource(R.string.settings_permissions),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp)
                )
                val entries = rememberPermissionEntries(app)
                entries.forEachIndexed { index, entry ->
                    if (index > 0) SoftDivider()
                    PermissionCheckRow(entry)
                }
            }
        }

        // Updates.
        SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_updates), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_update_notifications),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f)
                    )
                    AppSwitch(
                        checked = updateNotifications,
                        onCheckedChange = { scope.launch { app.settings.setUpdateNotifications(it) } }
                    )
                }
                GradientButton(
                    text = stringResource(
                        if (checking) R.string.settings_checking else R.string.settings_check_updates
                    ),
                    onClick = {
                        checking = true
                        scope.launch {
                            val info = app.updateRepository.checkNow(manual = true)
                            checking = false
                            if (info != null) {
                                onShowUpdate()
                            } else {
                                checkMessage = context.getString(
                                    R.string.settings_up_to_date, BuildConfig.VERSION_NAME
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 52,
                    enabled = !checking
                )
                checkMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Automation.
        SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_automation), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_automation_enable),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f)
                    )
                    val automationEnabled by app.settings.automationEnabled
                        .collectAsState(initial = false)
                    AppSwitch(
                        checked = automationEnabled,
                        onCheckedChange = { scope.launch { app.settings.setAutomationEnabled(it) } }
                    )
                }
                Text(
                    stringResource(R.string.settings_automation_desc),
                    fontSize = 13.sp,
                    color = b.muted
                )
                Text(
                    AutomationReceiver.ACTION_TRIGGER,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = b.text,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(b.field, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }

        // Logs row.
        SoftCard(onClick = onOpenLogs) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.logs_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = b.muted
                )
            }
        }
    }
}

/** Checklist row: icon bubble + title; gradient Enable or granted check. */
@Composable
private fun PermissionCheckRow(entry: PermissionEntry) {
    val b = burgundy()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(entry.icon, contentDescription = null)
        Text(
            stringResource(entry.titleRes),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (entry.granted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = b.accentText,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(R.string.perm_granted),
                    color = b.accentText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            GradientButton(
                text = stringResource(R.string.action_enable),
                onClick = entry.onEnable
            )
        }
    }
}
