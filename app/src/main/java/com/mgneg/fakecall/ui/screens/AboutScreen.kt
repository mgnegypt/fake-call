package com.mgneg.fakecall.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.BuildConfig
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import com.mgneg.fakecall.ui.components.SoftCard
import com.mgneg.fakecall.ui.components.SoftDivider
import com.mgneg.fakecall.ui.components.burgundy
import com.mgneg.fakecall.util.ContactLinks
import kotlinx.coroutines.launch

/**
 * The social/contact glyphs keep ONE fixed burgundy ink and bubble in
 * both themes (maintainer's rule): #8A1B3F on the pale brand tint, so
 * they never wash out or shift between light and dark mode.
 */
private val SocialInk = Color(0xFF8A1B3F)
private val SocialBubble = Color(0xFFF8DCE6)

/**
 * About, redesigned board: gradient hero with the app icon in a glass
 * tile, glass version/licence chips and a white update button; a trio
 * of stat cards straddling the hero edge; the developer card carrying
 * the contact row; purpose chips; an expandable disclaimer; the
 * project links as one divided card. Same handlers, URLs and licence
 * dialogs as before — presentation only.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(onShowUpdate: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCallApp
    val scope = rememberCoroutineScope()
    val b = burgundy()
    var showLicense by remember { mutableStateOf(false) }
    var showDeps by remember { mutableStateOf(false) }
    var disclaimerOpen by remember { mutableStateOf(false) }

    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---- Hero + stat trio (the trio straddles the hero's edge).
        Box(modifier = Modifier.fillMaxWidth()) {
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 44.dp, bottomEnd = 44.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFA02350), Color(0xFF4F0C24)))
                    )
            ) {
                // Decorative rings, zero-size so they never stretch the hero.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-70).dp)
                        .layout { m, c ->
                            val p = m.measure(c)
                            layout(0, 0) { p.place(0, 0) }
                        }
                        .size(340.dp)
                        .border(
                            1.dp, Color.White.copy(alpha = 0.10f), CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-10).dp)
                        .layout { m, c ->
                            val p = m.measure(c)
                            layout(0, 0) { p.place(0, 0) }
                        }
                        .size(220.dp)
                        .border(
                            1.dp, Color.White.copy(alpha = 0.16f), CircleShape
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp, end = 20.dp,
                            top = 34.dp, bottom = 64.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Glass tile with the launcher artwork.
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(
                                1.dp, Color.White.copy(alpha = 0.38f),
                                RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                R.drawable.ic_launcher_foreground
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                    Text(
                        stringResource(R.string.app_name),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "FAKE CALL",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassChip(
                            stringResource(
                                R.string.about_version,
                                BuildConfig.VERSION_NAME
                            )
                        )
                        GlassChip(stringResource(R.string.about_license))
                    }
                    // White update pill (fixed burgundy ink, both themes).
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .shadow(10.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .clickable(role = Role.Button) {
                                scope.launch {
                                    val info = app.updateRepository
                                        .checkNow(manual = true)
                                    if (info != null) onShowUpdate()
                                }
                            }
                            .padding(horizontal = 26.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            tint = SocialInk,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.settings_check_updates),
                            color = SocialInk,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            // Stat trio, overlapping the hero's bottom edge.
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 26.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(Icons.Filled.Code, R.string.about_stat_opensource)
                StatCard(Icons.Filled.Language, R.string.about_stat_bilingual)
                StatCard(Icons.Filled.WaterDrop, R.string.about_stat_material3)
            }
        }
        Spacer(Modifier.height(26.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---- Developer card, with the contact row inside.
            val contacts = listOf(
                Triple(
                    ContactLinks.FACEBOOK_URL, R.drawable.ic_brand_facebook,
                    R.string.contact_facebook
                ),
                Triple(
                    ContactLinks.WHATSAPP_URL, R.drawable.ic_brand_whatsapp,
                    R.string.contact_whatsapp
                ),
                Triple(
                    ContactLinks.INSTAGRAM_URL, R.drawable.ic_brand_instagram,
                    R.string.contact_instagram
                ),
                Triple(
                    ContactLinks.X_URL, R.drawable.ic_brand_x,
                    R.string.contact_x
                )
            ).filter { ContactLinks.isSet(it.first) }
            SoftCard(contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFA02350), Color(0xFF4F0C24))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "mg",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                        Column {
                            Text(
                                "mgneg",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                stringResource(R.string.about_dev_tagline),
                                fontSize = 12.sp,
                                color = b.muted
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.about_bio),
                        fontSize = 13.sp,
                        color = b.muted
                    )
                    SoftDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        contacts.forEach { (url, icon, label) ->
                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable(
                                    role = Role.Button
                                ) { open(url) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(SocialBubble),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        colorFilter =
                                            ColorFilter.tint(SocialInk)
                                    )
                                }
                                Text(
                                    stringResource(label),
                                    fontSize = 11.sp,
                                    color = b.text
                                )
                            }
                        }
                    }
                }
            }

            // ---- Purpose card with tag chips.
            SoftCard(contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.about_purpose_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.about_purpose),
                        fontSize = 13.sp,
                        color = b.muted
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TagPill(R.string.about_tag_privacy)
                        TagPill(R.string.about_tag_fun)
                        TagPill(R.string.about_tag_demos)
                        TagPill(R.string.about_tag_testing)
                    }
                }
            }

            // ---- Disclaimer: collapsed row, expands to the full text.
            SoftCard(
                contentPadding = PaddingValues(16.dp),
                onClick = { disclaimerOpen = !disclaimerOpen }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(b.danger.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = b.danger,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.about_disclaimer_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = b.danger
                            )
                            Text(
                                stringResource(R.string.about_disclaimer_more),
                                fontSize = 12.sp,
                                color = b.muted
                            )
                        }
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = b.muted,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(if (disclaimerOpen) 180f else 0f)
                        )
                    }
                    if (disclaimerOpen) {
                        Text(
                            stringResource(R.string.about_disclaimer),
                            fontSize = 13.sp,
                            color = b.muted
                        )
                    }
                }
            }

            // ---- Project links: one divided card.
            SoftCard(contentPadding = PaddingValues(vertical = 6.dp)) {
                Column {
                    LinkRow(Icons.Filled.Code, R.string.about_github) {
                        open(ContactLinks.GITHUB_REPO_URL)
                    }
                    SoftDivider(
                        modifier = Modifier.padding(start = 62.dp, end = 14.dp)
                    )
                    LinkRow(Icons.Filled.Shield, R.string.about_license) {
                        showLicense = true
                    }
                    SoftDivider(
                        modifier = Modifier.padding(start = 62.dp, end = 14.dp)
                    )
                    LinkRow(Icons.Filled.Description, R.string.about_licenses) {
                        showDeps = true
                    }
                }
            }

            Text(
                stringResource(R.string.about_footer),
                fontSize = 12.sp,
                color = b.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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

/** Glass pill on the hero gradient (board .gp). */
@Composable
private fun GlassChip(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/** Static tag pill inside the purpose card. */
@Composable
private fun TagPill(textRes: Int) {
    val b = burgundy()
    Text(
        stringResource(textRes),
        color = b.accentText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(b.tint)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/** One of the three stat cards straddling the hero edge. */
@Composable
private fun androidx.compose.foundation.layout.RowScope.StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textRes: Int
) {
    val b = burgundy()
    SoftCard(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(b.tint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = b.accentText,
                    modifier = Modifier.size(19.dp)
                )
            }
            Text(
                stringResource(textRes),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Icon-bubble row with a trailing chevron, inside the links card. */
@Composable
private fun LinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textRes: Int,
    onClick: () -> Unit
) {
    val b = burgundy()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(b.tint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = b.accentText,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            stringResource(textRes),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = b.muted,
            modifier = Modifier.size(20.dp)
        )
    }
}
