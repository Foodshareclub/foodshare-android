package com.foodshare.features.arrangement.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodshare.features.arrangement.domain.model.ArrangementStatus
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.LiquidGlassTypography
import com.foodshare.ui.design.tokens.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Status display card for arrangements.
 *
 * Features:
 * - Color-coded status badge
 * - Status text with description
 * - Timestamp of last update
 * - Animated transition between states
 */
@Composable
fun ArrangementStatusCard(
    status: ArrangementStatus,
    updatedAt: String?,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        isVisible = false
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(300),
        label = "statusScale"
    )

    val statusColor = getStatusColor(status)
    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(500),
        label = "statusColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = LiquidGlassGradients.glassSurface)
            .border(
                width = 1.dp,
                color = LiquidGlassColors.Glass.border,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(animatedColor)
                    )

                    Spacer(Modifier.width(Spacing.sm))

                    Text(
                        text = getStatusText(status),
                        style = LiquidGlassTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(Spacing.xs))

                Text(
                    text = getStatusDescription(status),
                    style = LiquidGlassTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                updatedAt?.let { timestamp ->
                    Spacer(Modifier.height(Spacing.xs))

                    Text(
                        text = formatTimestamp(timestamp),
                        style = LiquidGlassTypography.captionSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun getStatusColor(status: ArrangementStatus): Color {
    return when (status) {
        ArrangementStatus.PENDING -> LiquidGlassColors.warning
        ArrangementStatus.ACCEPTED -> LiquidGlassColors.brandBlue
        ArrangementStatus.DECLINED -> LiquidGlassColors.error
        ArrangementStatus.CONFIRMED -> LiquidGlassColors.brandCyan
        ArrangementStatus.COMPLETED -> LiquidGlassColors.success
        ArrangementStatus.CANCELLED -> LiquidGlassColors.error
        ArrangementStatus.NO_SHOW -> LiquidGlassColors.error
    }
}

private fun getStatusText(status: ArrangementStatus): String {
    return when (status) {
        ArrangementStatus.PENDING -> "Pending"
        ArrangementStatus.ACCEPTED -> "Accepted"
        ArrangementStatus.DECLINED -> "Declined"
        ArrangementStatus.CONFIRMED -> "Confirmed"
        ArrangementStatus.COMPLETED -> "Completed"
        ArrangementStatus.CANCELLED -> "Cancelled"
        ArrangementStatus.NO_SHOW -> "No Show"
    }
}

private fun getStatusDescription(status: ArrangementStatus): String {
    return when (status) {
        ArrangementStatus.PENDING -> "Waiting for owner to respond"
        ArrangementStatus.ACCEPTED -> "Owner has accepted your request"
        ArrangementStatus.DECLINED -> "Request was declined"
        ArrangementStatus.CONFIRMED -> "Pickup confirmed, ready to collect"
        ArrangementStatus.COMPLETED -> "Arrangement successfully completed"
        ArrangementStatus.CANCELLED -> "Arrangement was cancelled"
        ArrangementStatus.NO_SHOW -> "Marked as no-show"
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
            .withZone(ZoneId.systemDefault())
        "Updated ${formatter.format(instant)}"
    } catch (e: Exception) {
        "Updated recently"
    }
}
