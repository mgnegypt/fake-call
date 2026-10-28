package com.mgneg.fakecall.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.ui.components.BackHeader
import com.mgneg.fakecall.ui.components.GradientButton
import com.mgneg.fakecall.ui.components.HeroCard
import com.mgneg.fakecall.ui.components.IconBubble
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.burgundy

/**
 * Permissions screen (design board 05): summary hero + one card per
 * permission with its one-line reason and the same grant action the
 * Settings checklist uses. Opened from the Home header badge button.
 */
@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val b = burgundy()
    val entries = rememberPermissionEntries(app)
    val missing = entries.count { !it.granted && !it.unknown }
    val hasUnknown = entries.any { it.unknown }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackHeader(
            title = stringResource(R.string.settings_permissions),
            onBack = onBack,
            backContentDescription = stringResource(R.string.action_back)
        )

        HeroCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    if (missing > 0 || hasUnknown) Icons.Filled.WarningAmber else Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        if (missing > 0) {
                            pluralStringResource(R.plurals.permissions_need_enabling, missing, missing)
                        } else if (hasUnknown) {
                            stringResource(R.string.perm_account_unknown)
                        } else {
                            stringResource(R.string.permissions_all_granted)
                        },
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        stringResource(
                            if (missing > 0 || hasUnknown) R.string.permissions_hero_sub
                            else R.string.permissions_all_granted_sub
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        entries.forEach { entry ->
            SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconBubble(entry.icon, contentDescription = null)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(entry.titleRes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!entry.granted) {
                            Text(
                                stringResource(entry.reasonRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = b.muted
                            )
                        }
                    }
                    if (entry.granted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
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
                    } else if (entry.unknown) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                stringResource(R.string.perm_account_unknown),
                                color = b.muted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            GradientButton(
                                text = stringResource(R.string.action_try_call),
                                onClick = { entry.onTryCall?.invoke() }
                            )
                            androidx.compose.material3.TextButton(onClick = entry.onEnable) {
                                Text(stringResource(R.string.perm_open_settings))
                            }
                        }
                    } else {
                        GradientButton(
                            text = stringResource(R.string.action_enable),
                            onClick = entry.onEnable
                        )
                    }
                }
            }
        }
    }
}
