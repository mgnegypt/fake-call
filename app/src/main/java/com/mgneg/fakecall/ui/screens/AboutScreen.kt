package com.mgneg.fakecall.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.util.ContactLinks
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(onShowUpdate: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()
    var showLicense by remember { mutableStateOf(false) }
    var showDeps by remember { mutableStateOf(false) }

    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(com.mgneg.fakecall.ui.theme.BrandTile),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            }
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME))
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    val info = app.updateRepository.checkNow(manual = true)
                    if (info != null) onShowUpdate()
                }
            }) { Text(stringResource(R.string.settings_check_updates)) }
        }

        AboutCard(R.string.about_developer_title) {
            Text(stringResource(R.string.about_developed_by), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.about_bio), style = MaterialTheme.typography.bodyMedium)
        }
        AboutCard(R.string.about_purpose_title) {
            Text(stringResource(R.string.about_purpose), style = MaterialTheme.typography.bodyMedium)
        }
        AboutCard(R.string.about_disclaimer_title) {
            Text(stringResource(R.string.about_disclaimer), style = MaterialTheme.typography.bodyMedium)
        }

        Text(stringResource(R.string.about_contact_title), style = MaterialTheme.typography.titleMedium)
        val contacts = listOf(
            Triple(ContactLinks.FACEBOOK_URL, R.drawable.ic_brand_facebook, R.string.contact_facebook),
            Triple(ContactLinks.WHATSAPP_URL, R.drawable.ic_brand_whatsapp, R.string.contact_whatsapp),
            Triple(ContactLinks.INSTAGRAM_URL, R.drawable.ic_brand_instagram, R.string.contact_instagram),
            Triple(ContactLinks.X_URL, R.drawable.ic_brand_x, R.string.contact_x)
        )
        contacts.filter { ContactLinks.isSet(it.first) }.forEach { (url, icon, label) ->
            OutlinedButton(onClick = { open(url) }, modifier = Modifier.fillMaxWidth()) {
                Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(label), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }

        Text(stringResource(R.string.about_links_title), style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { open(ContactLinks.GITHUB_REPO_URL) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.about_github))
        }
        OutlinedButton(onClick = { showLicense = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.about_license))
        }
        OutlinedButton(onClick = { showDeps = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.about_licenses))
        }
        Spacer(Modifier.height(8.dp))
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

@Composable
private fun AboutCard(titleRes: Int, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
