package com.foodshare.features.forum.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.Spacing

/**
 * A compact composable showing the trust level as a colored badge.
 *
 * Displays a level-appropriate icon alongside a short name, with a
 * background colored from the trust level's hex color string.
 *
 * SYNC: This mirrors the iOS trust level badge display.
 */
@Composable
fun TrustLevelBadge(
    level: Int,
    name: String,
    color: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = parseHexColor(color)
    val icon = trustLevelIcon(level)
    val displayName = name.ifEmpty { trustLevelShortName(level) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.xs))
            .background(backgroundColor.copy(alpha = 0.85f))
            .padding(horizontal = Spacing.xs, vertical = Spacing.xxxs),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Trust Level $level",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = displayName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * Returns the Material icon for a given trust level.
 */
private fun trustLevelIcon(level: Int): ImageVector = when (level) {
    0 -> Icons.Default.Person
    1 -> Icons.Default.Verified
    2 -> Icons.Default.Star
    3 -> Icons.Default.WorkspacePremium
    4 -> Icons.Default.EmojiEvents
    else -> Icons.Default.Person
}

/**
 * Returns the short name for a given trust level.
 */
private fun trustLevelShortName(level: Int): String = when (level) {
    0 -> "New"
    1 -> "Basic"
    2 -> "Member"
    3 -> "Regular"
    4 -> "Leader"
    else -> "L$level"
}

/**
 * Parses a hex color string (e.g. "#60A5FA") into a Compose Color.
 * Falls back to gray on parse failure.
 */
private fun parseHexColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorLong = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000 or colorLong)
            8 -> Color(colorLong)
            else -> Color(0xFF9CA3AF)
        }
    } catch (_: Exception) {
        Color(0xFF9CA3AF)
    }
}
