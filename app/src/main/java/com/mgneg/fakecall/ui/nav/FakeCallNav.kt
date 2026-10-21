package com.mgneg.fakecall.ui.nav

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.telecom.CallPath
import com.mgneg.fakecall.telecom.CallState
import com.mgneg.fakecall.telecom.IncomingCallActivity
import com.mgneg.fakecall.ui.components.AppBackground
import com.mgneg.fakecall.ui.components.FloatingNavBar
import com.mgneg.fakecall.ui.components.NavSlot
import com.mgneg.fakecall.ui.screens.AboutScreen
import com.mgneg.fakecall.ui.screens.HistoryScreen
import com.mgneg.fakecall.ui.screens.HomeScreen
import com.mgneg.fakecall.ui.screens.LogsScreen
import com.mgneg.fakecall.ui.screens.PermissionsScreen
import com.mgneg.fakecall.ui.screens.SettingsScreen
import com.mgneg.fakecall.ui.screens.UpdateSheet
import com.mgneg.fakecall.ui.screens.WhatsNewSheet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    /** Sub-destination of Settings (the diagnostics logs screen). */
    const val LOGS = "logs"

    /** Sub-destination of Home (the permissions screen, design board 05). */
    const val PERMISSIONS = "permissions"
}

@Composable
fun FakeCallRoot(app: FakeCallApp) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: Routes.HOME

    val updateInfo by app.updateRepository.available.collectAsState()
    var showUpdate by remember { mutableStateOf(false) }
    var whatsNew by remember { mutableStateOf<Pair<String, String>?>(null) }

    // The floating bar's FAB is the existing "Call now" action. While
    // Home is composed it registers its exact handler (current form
    // values); from other tabs the FAB falls back to the stored default
    // caller with no delay — what Home itself would dial after a tab
    // round-trip reloads the form from those same defaults.
    var callNowHandler by remember { mutableStateOf<(() -> Unit)?>(null) }
    val onFabClick: () -> Unit = {
        val handler = callNowHandler
        if (handler != null) {
            handler()
        } else {
            scope.launch {
                app.callRepository.triggerCall(
                    app.settings.defaultName.first(),
                    app.settings.defaultNumber.first(),
                    null,
                    0
                )
            }
        }
    }

    // Surface OUR call UI only for a live call on the fallback
    // (app-screen) path. On the Telecom path the system phone app owns
    // the call UI end to end — launching our activity there would stack
    // it on top of the system's presentation (the overlap bug this
    // design explicitly forbids; see docs/CALL_ENGINE.md). The app
    // root only needs to bring the fallback screen forward when the
    // app is already composed, e.g. a "Call Now" tap.
    val callState by app.callController.state.collectAsState()
    var surfacedFor by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(callState) {
        val liveId = when (val s = callState) {
            is CallState.Ringing -> s.historyId.takeIf { s.path == CallPath.APP_SCREEN }
            is CallState.InCall -> s.historyId.takeIf { s.path == CallPath.APP_SCREEN }
            is CallState.Idle -> null
        }
        if (liveId == null) {
            surfacedFor = null
        } else if (surfacedFor != liveId) {
            surfacedFor = liveId
            context.startActivity(
                Intent(context, IncomingCallActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
    }

    // "What's new" once after an update + background check on start.
    LaunchedEffect(Unit) {
        app.updateRepository.checkNow(manual = false)
        val lastSeen = app.settings.lastSeenVersion.first()
        val current = BuildConfig.VERSION_NAME
        if (lastSeen.isNotEmpty() && lastSeen != current) {
            val cache = app.settings.updateCache.first()
            if (cache.latestVersion == current && cache.notes.isNotBlank()) {
                whatsNew = current to cache.notes
            }
        }
        if (lastSeen != current && whatsNew == null) {
            app.settings.setLastSeenVersion(current)
        }
    }

    val mainRoutes = listOf(Routes.HOME, Routes.HISTORY, Routes.SETTINGS, Routes.ABOUT)
    val slots = listOf(
        NavSlot(Routes.HOME, stringResource(R.string.nav_home), Icons.Outlined.Home),
        NavSlot(Routes.HISTORY, stringResource(R.string.nav_history), Icons.Outlined.History),
        NavSlot(
            Routes.SETTINGS, stringResource(R.string.nav_settings), Icons.Outlined.Tune,
            showDot = updateInfo != null
        ),
        NavSlot(Routes.ABOUT, stringResource(R.string.nav_about), Icons.Outlined.Info)
    )
    // Logs keeps the Settings tab highlighted, as before.
    val highlightedRoute = if (route == Routes.LOGS) Routes.SETTINGS else route

    AppBackground {
        // The pre-redesign Scaffold applied the system-bar insets; with
        // targetSdk 37 the window is edge-to-edge, so pad the content
        // (and the floating bar) inside the full-bleed background.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomeScreen(
                        snackbar = snackbar,
                        onOpenPermissions = { navController.navigate(Routes.PERMISSIONS) },
                        onCallNowReady = { callNowHandler = it }
                    )
                }
                composable(Routes.HISTORY) {
                    HistoryScreen(onScheduleCall = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onShowUpdate = { showUpdate = true },
                        onOpenLogs = { navController.navigate(Routes.LOGS) }
                    )
                }
                composable(Routes.LOGS) { LogsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.PERMISSIONS) {
                    PermissionsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ABOUT) { AboutScreen(onShowUpdate = { showUpdate = true }) }
            }

            SnackbarHost(
                snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 104.dp)
            )

            // Hidden on sub-screens (Logs, Permissions) per the design.
            if (route in mainRoutes) {
                FloatingNavBar(
                    slots = slots,
                    currentRoute = highlightedRoute,
                    onNavigate = { dest ->
                        navController.navigate(dest) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onFabClick = onFabClick,
                    fabContentDescription = stringResource(R.string.call_now_button),
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (showUpdate) {
        updateInfo?.let { info ->
            UpdateSheet(info = info, onDismiss = { showUpdate = false })
        }
    }
    whatsNew?.let { (version, notes) ->
        WhatsNewSheet(version = version, notesBody = notes) {
            whatsNew = null
            scope.launch { app.settings.setLastSeenVersion(BuildConfig.VERSION_NAME) }
        }
    }
}
