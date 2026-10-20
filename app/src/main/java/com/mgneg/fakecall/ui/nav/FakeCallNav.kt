package com.mgneg.fakecall.ui.nav

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.mgneg.fakecall.ui.screens.AboutScreen
import com.mgneg.fakecall.ui.screens.HistoryScreen
import com.mgneg.fakecall.ui.screens.HomeScreen
import com.mgneg.fakecall.ui.screens.LogsScreen
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
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val items = listOf(
        Triple(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
        Triple(Routes.HISTORY, R.string.nav_history, Icons.Filled.History),
        Triple(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings),
        Triple(Routes.ABOUT, R.string.nav_about, Icons.Filled.Info)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (updateInfo != null) {
                        IconButton(onClick = { showUpdate = true }) {
                            BadgedBox(badge = { Badge() }) {
                                Icon(
                                    Icons.Filled.SystemUpdate,
                                    contentDescription = stringResource(R.string.update_title)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { (dest, label, icon) ->
                    NavigationBarItem(
                        // Logs is a sub-screen of Settings: keep the
                        // Settings tab highlighted while it is showing.
                        selected = route == dest ||
                            (dest == Routes.SETTINGS && route == Routes.LOGS),
                        onClick = {
                            navController.navigate(dest) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (dest == Routes.SETTINGS && updateInfo != null) {
                                BadgedBox(badge = { Badge(modifier = Modifier.size(8.dp)) }) {
                                    Icon(icon, contentDescription = null)
                                }
                            } else {
                                Icon(icon, contentDescription = null)
                            }
                        },
                        label = { Text(stringResource(label)) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomeScreen(
                        snackbar = snackbar,
                        updateInfo = updateInfo,
                        onShowUpdate = { showUpdate = true }
                    )
                }
                composable(Routes.HISTORY) { HistoryScreen() }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onShowUpdate = { showUpdate = true },
                        onOpenLogs = { navController.navigate(Routes.LOGS) }
                    )
                }
                composable(Routes.LOGS) { LogsScreen() }
                composable(Routes.ABOUT) { AboutScreen(onShowUpdate = { showUpdate = true }) }
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
