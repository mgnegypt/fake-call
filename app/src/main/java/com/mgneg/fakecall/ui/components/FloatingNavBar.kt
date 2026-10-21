package com.mgneg.fakecall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class NavSlot(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val showDot: Boolean = false
)

/**
 * Floating glass bottom bar (design .nav): Home, History, centre FAB
 * (the existing "Call now" action), Settings, About. The active tab
 * shows its icon on a tint pill; all tabs are icon-only, with the
 * label kept as the content description. Rendered as an overlay;
 * screens reserve
 * bottom space for it.
 */
@Composable
fun FloatingNavBar(
    slots: List<NavSlot>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit,
    fabContentDescription: String,
    modifier: Modifier = Modifier
) {
    require(slots.size == 4) { "FloatingNavBar expects exactly 4 tabs around the FAB" }
    val b = burgundy()
    val barShape = RoundedCornerShape(32.dp)
    Box(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(14.dp, barShape, ambientColor = b.shadowSoft, spotColor = b.shadowSoft)
                .clip(barShape)
                .background(b.nav)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(slots[0], currentRoute, onNavigate, Modifier.weight(1f))
            NavItem(slots[1], currentRoute, onNavigate, Modifier.weight(1f))
            // Centre slot reserved for the raised FAB.
            Box(modifier = Modifier.weight(1f))
            NavItem(slots[2], currentRoute, onNavigate, Modifier.weight(1f))
            NavItem(slots[3], currentRoute, onNavigate, Modifier.weight(1f))
        }
        // FAB: 60dp gradient circle, 4dp ring, raised above the bar.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .size(60.dp)
                .shadow(10.dp, CircleShape, ambientColor = b.shadowStrong, spotColor = b.shadowStrong)
                .border(4.dp, b.fabRing, CircleShape)
                .clip(CircleShape)
                .background(primaryGradient())
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Bolt,
                contentDescription = fabContentDescription,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    slot: NavSlot,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val b = burgundy()
    val active = slot.route == currentRoute
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .then(if (active) Modifier.background(b.tint) else Modifier)
                .selectable(
                    selected = active,
                    role = androidx.compose.ui.semantics.Role.Tab
                ) { onNavigate(slot.route) }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    slot.icon,
                    // Icons only in the bar (no text labels); the label
                    // stays as the content description for TalkBack.
                    contentDescription = slot.label,
                    tint = if (active) b.accentText else b.muted,
                    modifier = Modifier.size(22.dp)
                )
                if (slot.showDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(b.accent)
                    )
                }
            }
        }
    }
}
