package com.foodshare.ui.design.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassTypography
import com.foodshare.ui.design.tokens.Spacing

/**
 * Glass stat card for displaying metrics and statistics
 *
 * Features:
 * - Large value display in headlineSmall bold
 * - Label in labelSmall
 * - Optional trend indicator with up/down arrow
 * - Trend percentage with customizable color
 * - Centered column layout in GlassCard wrapper
 */
@Composable
fun GlassStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    trend: Float? = null,
    trendColor: Color = LiquidGlassColors.success
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = CornerRadius.medium,
        shadow = GlassShadow.Medium
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Value display
            Text(
                text = value,
                style = LiquidGlassTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = LiquidGlassColors.Text.primary,
                textAlign = TextAlign.Center
            )

            // Label
            Text(
                text = label,
                style = LiquidGlassTypography.labelSmall,
                color = LiquidGlassColors.Text.secondary,
                textAlign = TextAlign.Center
            )

            // Trend indicator
            if (trend != null) {
                Spacer(modifier = Modifier.size(Spacing.xxs))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (trend >= 0) {
                            Icons.Default.ArrowUpward
                        } else {
                            Icons.Default.ArrowDownward
                        },
                        contentDescription = if (trend >= 0) "Increase" else "Decrease",
                        tint = if (trend >= 0) {
                            trendColor
                        } else {
                            LiquidGlassColors.error
                        },
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${if (trend >= 0) "+" else ""}%.1f%%".format(trend),
                        style = LiquidGlassTypography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (trend >= 0) {
                            trendColor
                        } else {
                            LiquidGlassColors.error
                        }
                    )
                }
            }
        }
    }
}
