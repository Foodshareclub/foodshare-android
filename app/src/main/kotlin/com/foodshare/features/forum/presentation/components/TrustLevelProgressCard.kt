package com.foodshare.features.forum.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodshare.features.forum.domain.model.RequirementProgress
import com.foodshare.features.forum.domain.model.TrustLevelProgress
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.Spacing

/**
 * A GlassCard showing progress towards the next trust level.
 *
 * Displays the level name, an overall progress bar, and lists
 * individual requirements with their progress indicators.
 *
 * SYNC: This mirrors the iOS trust level progress display.
 */
@Composable
fun TrustLevelProgressCard(
    progress: TrustLevelProgress,
    modifier: Modifier = Modifier
) {
    val trustLevel = progress.trustLevel
    val overallPercent = (progress.overallProgress * 100).toInt()

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            // Header: Level name + percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Next Level",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = trustLevel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "$overallPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (progress.isComplete) {
                        LiquidGlassColors.success
                    } else {
                        Color.White
                    }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Overall progress bar
            LinearProgressIndicator(
                progress = { progress.overallProgress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress.isComplete) {
                    LiquidGlassColors.success
                } else {
                    LiquidGlassColors.brandPink
                },
                trackColor = LiquidGlassColors.Glass.background,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Individual requirements
            progress.requirements.forEach { requirement ->
                if (requirement.required > 0) {
                    RequirementRow(requirement = requirement)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                }
            }
        }
    }
}

@Composable
private fun RequirementRow(
    requirement: RequirementProgress
) {
    val icon = requirementIcon(requirement.icon)
    val progressColor = if (requirement.isMet) {
        LiquidGlassColors.success
    } else {
        LiquidGlassColors.brandPink
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(LiquidGlassColors.Glass.extraSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (requirement.isMet) {
                    LiquidGlassColors.success
                } else {
                    Color.White.copy(alpha = 0.7f)
                },
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.xs))

        // Name + progress text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = requirement.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = requirement.displayText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        // Mini progress bar
        LinearProgressIndicator(
            progress = { requirement.progress.toFloat() },
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = progressColor,
            trackColor = LiquidGlassColors.Glass.background,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Maps requirement icon names to Material icons.
 */
private fun requirementIcon(name: String): ImageVector = when (name) {
    "calendar_today" -> Icons.Default.CalendarToday
    "visibility" -> Icons.Default.Visibility
    "description" -> Icons.Default.Description
    "edit" -> Icons.Default.Edit
    "thumb_up" -> Icons.Default.ThumbUp
    "favorite" -> Icons.Default.Favorite
    "schedule" -> Icons.Default.Schedule
    else -> Icons.Default.Schedule
}
