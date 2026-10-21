package com.mgneg.fakecall.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.ui.components.GradientButton
import com.mgneg.fakecall.ui.components.GradientTile
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.SoftChip
import com.mgneg.fakecall.ui.components.burgundy
import com.mgneg.fakecall.util.ContactLinks
import kotlinx.coroutines.launch

/**
 * About, burgundy layout: same strings, same URLs, same licence
 * dialogs — only the presentation changed (board 10).
 */
@Composable
fun AboutScreen(onShowUpdate: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()
    val b = burgundy()
    var showLicense by remember { mutableStateOf(false) }
    var showDeps by remember { mutableStateOf(false) }

    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GradientTile(
                Icons.Filled.Call,
                contentDescription = null,
                size = 84,
                radius = 26,
                iconSize = 40
            )
            Text(
                stringResource(R.string.app_name),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = b.muted
            )
            GradientButton(
                text = stringResource(R.string.settings_check_updates),
                onClick = {
                    scope.launch {
                        val info = app.updateRepository.checkNow(manual = true)
                        if (info != null) onShowUpdate()
                    }
                },
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        AboutCard(R.string.about_developer_title) {
            Text(stringResource(R.string.about_developed_by), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.about_bio),
                fontSize = 13.sp,
                color = b.muted
            )
        }
        AboutCard(R.string.about_purpose_title) {
            Text(
                stringResource(R.string.about_purpose),
                fontSize = 13.sp,
                color = b.muted
            )
        }
        SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.about_disclaimer_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = b.danger
                )
                Text(
                    stringResource(R.string.about_disclaimer),
                    fontSize = 13.sp,
                    color = b.muted
                )
            }
        }

        Text(stringResource(R.string.about_contact_title), style = MaterialTheme.typography.titleMedium)
        val contacts = listOf(
            Triple(ContactLinks.FACEBOOK_URL, R.drawable.ic_brand_facebook, R.string.contact_facebook),
            Triple(ContactLinks.WHATSAPP_URL, R.drawable.ic_brand_whatsapp, R.string.contact_whatsapp),
            Triple(ContactLinks.INSTAGRAM_URL, R.drawable.ic_brand_instagram, R.string.contact_instagram),
            Triple(ContactLinks.X_URL, R.drawable.ic_brand_x, R.string.contact_x)
        ).filter { ContactLinks.isSet(it.first) }
        // Two-column link grid (board .lk cards).
        contacts.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { (url, icon, label) ->
                    SoftCard(
                        modifier = Modifier.weight(1f),
                        onClick = { open(url) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box30Icon(icon)
                            Text(
                                stringResource(label),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
        }

        Text(stringResource(R.string.about_links_title), style = MaterialTheme.typography.titleMedium)
        // The board makes this row scrollable horizontally: the three
        // labels (especially in English) exceed a 360dp screen.
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            SoftChip(
                text = stringResource(R.string.about_github),
                selected = false,
                onClick = { open(ContactLinks.GITHUB_REPO_URL) }
            )
            SoftChip(
                text = stringResource(R.string.about_license),
                selected = false,
                onClick = { showLicense = true }
            )
            SoftChip(
                text = stringResource(R.string.about_licenses),
                selected = false,
                onClick = { showDeps = true }
            )
        }
    }

    if (showLicense) {
        AlertDialog(
            onDismissRequest = { showLicense = false },
            title = { Text(stringResource(R.string.about_license)) },
            text = {
                Text(
                    "MIT License\n\nCopyright (c) 2026 mgneg\n\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy " +
                        "of this software and associated documentation files (the \"Software\"), to deal " +
                        "in the Software without restriction, subject to the conditions in the LICENSE " +
                        "file shipped with the source code.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicense = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        )
    }
    if (showDeps) {
        AlertDialog(
            onDismissRequest = { showDeps = false },
            title = { Text(stringResource(R.string.about_licenses)) },
            text = {
                Text(
                    "AndroidX & Jetpack Compose — Apache-2.0\n" +
                        "Kotlin & kotlinx — Apache-2.0\n" +
                        "Material Components — Apache-2.0\n" +
                        "Simple Icons (brand logos) — CC0\n\n" +
                        "Full list: THIRD_PARTY_NOTICES.md in the source repository.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeps = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        )
    }
}

/** 30dp tint bubble holding a brand drawable (board .lk icon). */
@Composable
private fun Box30Icon(drawableRes: Int) {
    val b = burgundy()
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(30.dp)
            .background(b.tint, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AboutCard(titleRes: Int, content: @Composable () -> Unit) {
    SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
