package com.mgneg.fakecall.ui.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.ui.screens.AboutScreen
import com.mgneg.fakecall.ui.screens.HistoryScreen
import com.mgneg.fakecall.ui.screens.HomeScreen
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeCallRoot(app: FakeCallApp) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: Routes.HOME

    val updateInfo by app.updateRepository.available.collectAsState()
    var showUpdate by remember { mutableStateOf(false) }
    var whatsNew by remember { mutableStateOf<Pair<String, String>?>(null) }

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

    val title = when (route) {
        Routes.HISTORY -> stringResource(R.string.history_title)
        Routes.SETTINGS -> stringResource(R.string.settings_title)
        Routes.ABOUT -> stringResource(R.string.about_title)
        else -> stringResource(R.string.app_name)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(androidx.compose.ui.graphics.Color(0xFF0B3D5C)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                HorizontalDivider()
                DrawerItem(
                    label = stringResource(R.string.nav_home),
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    selected = route == Routes.HOME
                ) { navigateTo(Routes.HOME, navController, scope, drawerState) }
                DrawerItem(
                    label = stringResource(R.string.nav_history),
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    selected = route == Routes.HISTORY
                ) { navigateTo(Routes.HISTORY, navController, scope, drawerState) }
                DrawerItem(
                    label = stringResource(R.string.nav_settings),
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    selected = route == Routes.SETTINGS
                ) { navigateTo(Routes.SETTINGS, navController, scope, drawerState) }
                DrawerItem(
                    label = stringResource(R.string.nav_about),
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    selected = route == Routes.ABOUT
                ) { navigateTo(Routes.ABOUT, navController, scope, drawerState) }
                updateInfo?.let { info ->
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = {
                            Text(stringResource(R.string.nav_update_available, info.version.toString()))
                        },
                        icon = { Icon(Icons.Filled.SystemUpdate, contentDescription = null) },
                        badge = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showUpdate = true
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.nav_open_drawer)
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) { HomeScreen(snackbar) }
                    composable(Routes.HISTORY) { HistoryScreen() }
                    composable(Routes.SETTINGS) { SettingsScreen(onShowUpdate = { showUpdate = true }) }
                    composable(Routes.ABOUT) { AboutScreen(onShowUpdate = { showUpdate = true }) }
                }
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

@Composable
private fun DrawerItem(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = icon,
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}

private fun navigateTo(
    route: String,
    navController: androidx.navigation.NavController,
    scope: kotlinx.coroutines.CoroutineScope,
    drawerState: androidx.compose.material3.DrawerState
) {
    scope.launch { drawerState.close() }
    navController.navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
