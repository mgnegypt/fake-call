package com.mgneg.fakecall.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgneg.fakecall.ui.theme.BurgundyColors
import com.mgneg.fakecall.ui.theme.LocalBurgundy

/** The 135° primary gradient (accentDark → accent), white content. */
fun primaryGradient(): Brush =
    Brush.linearGradient(listOf(Color(0xFF4F0C24), Color(0xFF8A1B3F)))

@Composable
fun burgundy(): BurgundyColors = LocalBurgundy.current

/** Page background: 170° gradient + radial glow at the top-end corner. */
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    val b = burgundy()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(b.bgTop, b.bgBottom)))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(360.dp)
                .offset(x = 120.dp, y = (-120).dp)
                .background(
                    Brush.radialGradient(listOf(b.glow, Color.Transparent))
                )
        )
        content()
    }
}

/** White/translucent rounded card with the soft burgundy shadow. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    radius: Int = 24,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val b = burgundy()
    val shape = RoundedCornerShape(radius.dp)
    var box = Modifier
        .shadow(10.dp, shape, ambientColor = b.shadowSoft, spotColor = b.shadowSoft)
        .clip(shape)
        .background(b.card)
    if (onClick != null) box = box.clickable(onClick = onClick)
    Box(modifier = modifier.then(box).padding(contentPadding)) { content() }
}

/** Gradient hero surface with the soft white highlight at top-start. */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit
) {
    val b = burgundy()
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .shadow(16.dp, shape, ambientColor = b.shadowStrong, spotColor = b.shadowStrong)
            .clip(shape)
            .background(primaryGradient())
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(220.dp)
                .offset(x = (-60).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
        )
        Box(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

/** Circular tint bubble with an accent icon (36 rows / 46 history / 52 avatar). */
@Composable
fun IconBubble(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Int = 36,
    iconSize: Int = 20
) {
    val b = burgundy()
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(b.tint),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = b.accentText,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

/** Logo tile: primary gradient rounded square with a white icon. */
@Composable
fun GradientTile(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Int = 38,
    radius: Int = 12,
    iconSize: Int = 20
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(radius.dp))
            .background(primaryGradient()),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

/** Icon + title + optional subtitle + trailing slot, min height 56. */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    var row = modifier.fillMaxWidth()
    if (onClick != null) row = row.clickable(onClick = onClick)
    Row(
        modifier = row.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(icon, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = burgundy().muted
                )
            }
        }
        trailing()
    }
}

/** Pill gradient button. [height] 44 default; the big CTA uses 60. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 44,
    textSize: Int = 14,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape((height / 2).dp))
            .background(primaryGradient())
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text,
                color = Color.White,
                fontSize = textSize.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Card-style chip; gradient when selected (delay chips, filters). */
@Composable
fun SoftChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 44
) {
    val b = burgundy()
    val shape = RoundedCornerShape((height / 2).dp)
    var chip = Modifier
        .height(height.dp)
        .shadow(6.dp, shape, ambientColor = b.shadowSoft, spotColor = b.shadowSoft)
        .clip(shape)
    chip = if (selected) chip.background(primaryGradient())
    else chip.background(b.card)
    Box(
        modifier = modifier.then(chip).clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else b.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Chip for use on the gradient hero: translucent white, solid white when on. */
@Composable
fun HeroChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF8A1B3F) else Color.White,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
        )
    }
}

/** Segmented control on a field track; selected segment is the gradient. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val b = burgundy()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(b.field)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .then(
                        if (selected) Modifier.background(primaryGradient())
                        else Modifier
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) Color.White else b.muted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Small label above a value, on the field fill (the design's .fd). */
@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    valueTextStyle: TextStyle? = null,
    singleLine: Boolean = true,
    /** Phone-number style values: force LTR direction inside the field. */
    ltrValue: Boolean = false
) {
    val b = burgundy()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(b.field)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = b.muted)
        val field: @Composable () -> Unit = {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            textStyle = (valueTextStyle ?: MaterialTheme.typography.bodyLarge)
                .copy(color = b.text, textAlign = TextAlign.Start),
            cursorBrush = SolidColor(b.accent),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            placeholder,
                            style = valueTextStyle ?: MaterialTheme.typography.bodyLarge,
                            color = b.muted.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    inner()
                }
            }
        )
        }
        if (ltrValue) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides
                    androidx.compose.ui.unit.LayoutDirection.Ltr
            ) { field() }
        } else {
            field()
        }
    }
}

/** 52×30 switch with a white knob (the design's .sw). */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val b = burgundy()
    // The knob travels from the start edge to the end edge; the offset
    // must flip sign in RTL or the knob would leave the track.
    val rtl = androidx.compose.ui.platform.LocalLayoutDirection.current ==
        androidx.compose.ui.unit.LayoutDirection.Rtl
    val knobTravel by animateDpAsState(
        targetValue = if (checked) (if (rtl) (-22).dp else 22.dp) else 0.dp,
        label = "switchKnob"
    )
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked) b.accent else b.divider)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (if (rtl) (-4).dp else 4.dp) + knobTravel)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** Row with [AppSwitch] trailing; whole row toggles. */
@Composable
fun SwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    SettingRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        onClick = { onCheckedChange(!checked) }
    ) {
        AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Pill dropdown on the field fill; menu on card2 with tint + check. */
@Composable
fun DropdownField(
    value: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val b = burgundy()
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(b.field)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = b.text,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = b.muted,
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(b.card2)
        ) {
            options.forEachIndexed { index, option ->
                val selected = option == value
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) b.accentText else b.text
                        )
                    },
                    trailingIcon = if (selected) {
                        {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = b.accentText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        expanded = false
                        onSelect(index)
                    },
                    modifier = if (selected) {
                        Modifier.background(b.tint, RoundedCornerShape(14.dp))
                    } else Modifier
                )
            }
        }
    }
}

enum class PillKind { Tint, Danger, Muted }

/** Small status pill (answered / declined / completed / failed…). */
@Composable
fun StatusPill(text: String, kind: PillKind, modifier: Modifier = Modifier) {
    val b = burgundy()
    val (bg, fg) = when (kind) {
        PillKind.Tint -> b.tint to b.accentText
        PillKind.Danger -> b.dangerBg to b.danger
        PillKind.Muted -> b.field to b.muted
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

/** Circular back button + screen title for sub-screens. */
@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    backContentDescription: String,
    modifier: Modifier = Modifier
) {
    val b = burgundy()
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SoftCard(
            radius = 23,
            modifier = Modifier.size(46.dp),
            onClick = onBack
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backContentDescription,
                    tint = b.text,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

/** Key/value row used by the Logs cards. */
@Composable
fun KeyValueRow(label: String, value: String, modifier: Modifier = Modifier) {
    val b = burgundy()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = b.muted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = b.text)
    }
}

/** 1px divider in the design divider colour, inset like the boards. */
@Composable
fun SoftDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(burgundy().divider)
    )
}
