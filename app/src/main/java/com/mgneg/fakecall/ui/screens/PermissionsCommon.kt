package com.mgneg.fakecall.ui.screens

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.StayCurrentPortrait
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.telecom.AccountEnablement
import com.mgneg.fakecall.telecom.TelecomAccounts
import kotlinx.coroutines.launch
import com.mgneg.fakecall.util.Permissions

/** One permission row: live state + the fastest grant path. */
data class PermissionEntry(
    val icon: ImageVector,
    val titleRes: Int,
    val reasonRes: Int,
    val granted: Boolean,
    val onEnable: () -> Unit,
    /** State could not be verified (phone account only): not granted,
     * but NOT counted as missing either — the honest UNKNOWN state. */
    val unknown: Boolean = false,
    /** Phone account only: place an immediate test call to produce
     * connection evidence when the state is [unknown]. */
    val onTryCall: (() -> Unit)? = null
)

/**
 * The app's single permission set (6 items) with live state, re-checked
 * on ON_RESUME. Shared by the Settings checklist, the Permissions screen
 * and the Home header badge so all three always agree. The checks and
 * grant actions are the exact ones the pre-redesign Settings checklist
 * and Home readiness cards used.
 *
 * READ_PHONE_STATE is deliberately NOT part of the set (removed in 1.3):
 * registering our own phone account and placing calls on it never
 * required it. One diagnostics-only probe (callCapablePhoneAccounts) is
 * permission-gated by the platform; it is called best-effort without the
 * permission and simply reports "unavailable" (see TelecomAccounts).
 *
 * Because that probe is also the only authoritative enablement check,
 * the phone-account row is tri-state (owner decision 2026-10-28):
 * ENABLED / DISABLED / UNKNOWN per AccountEnablementRules, where stored
 * connection evidence (the system actually delivered a call to our
 * ConnectionService) counts as ENABLED, and UNKNOWN is never reported
 * as "needs enabling".
 */
@Composable
fun rememberPermissionEntries(app: FakeCallApp): List<PermissionEntry> {
    val context = LocalContext.current
    val activity = context as? Activity

    val scope = rememberCoroutineScope()
    val evidence by app.settings.telecomEvidence.collectAsState(initial = false)
    var accountState by remember { mutableStateOf(AccountEnablement.UNKNOWN) }
    var exactOk by remember { mutableStateOf(true) }
    var notifOk by remember { mutableStateOf(true) }
    var fsOk by remember { mutableStateOf(true) }
    var batteryOk by remember { mutableStateOf(true) }
    var installOk by remember { mutableStateOf(true) }

    fun refresh() {
        accountState = TelecomAccounts.enablement(context, evidence)
        exactOk = Permissions.canScheduleExact(context)
        notifOk = Permissions.notificationsEnabled(context)
        fsOk = Permissions.canFullScreenIntent(context)
        batteryOk = Permissions.isIgnoringBatteryOptimizations(context)
        installOk = Permissions.canInstallUnknownApps(context)
    }
    LaunchedEffect(Unit) { refresh() }
    LaunchedEffect(evidence) { refresh() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refresh() }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    return listOf(
        PermissionEntry(
            icon = Icons.Outlined.Call,
            titleRes = R.string.account_card_title,
            reasonRes = R.string.perm_reason_account,
            granted = accountState == AccountEnablement.ENABLED,
            onEnable = {
                activity?.startActivity(TelecomAccounts.accountSettingsIntent())
            },
            unknown = accountState == AccountEnablement.UNKNOWN,
            onTryCall = {
                scope.launch {
                    runCatching { app.callRepository.triggerCall(null, null, null, 0) }
                }
            }
        ),
        PermissionEntry(
            icon = Icons.Outlined.Alarm,
            titleRes = R.string.exact_alarm_card_title,
            reasonRes = R.string.perm_reason_exact,
            granted = exactOk,
            onEnable = { activity?.startActivity(Permissions.exactAlarmSettingsIntent(context)) }
        ),
        PermissionEntry(
            icon = Icons.Outlined.Notifications,
            titleRes = R.string.notifications_card_title,
            reasonRes = R.string.perm_reason_notifications,
            granted = notifOk,
            onEnable = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    activity?.startActivity(Permissions.notificationSettingsIntent(context))
                }
            }
        ),
        PermissionEntry(
            icon = Icons.Outlined.StayCurrentPortrait,
            titleRes = R.string.fullscreen_card_title,
            reasonRes = R.string.perm_reason_fullscreen,
            granted = fsOk,
            onEnable = { activity?.startActivity(Permissions.fullScreenSettingsIntent(context)) }
        ),
        PermissionEntry(
            icon = Icons.Outlined.BatteryChargingFull,
            titleRes = R.string.perm_battery,
            reasonRes = R.string.perm_reason_battery,
            granted = batteryOk,
            onEnable = { activity?.startActivity(Permissions.batteryOptimizationIntent(context)) }
        ),
        PermissionEntry(
            icon = Icons.Outlined.Download,
            titleRes = R.string.perm_install_updates,
            reasonRes = R.string.perm_reason_install,
            granted = installOk,
            onEnable = { activity?.startActivity(Permissions.installPermissionIntent(context)) }
        )
    )
}
