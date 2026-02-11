package com.foodshare.features.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Settings section with header and content
 *
 * Features:
 * - Section title in brand pink
 * - Glass card container
 * - Content arranged vertically
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Section title
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = LiquidGlassColors.brandPink,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = Spacing.xxs)
        )

        // Content in glass card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerRadius.medium))
                .background(brush = LiquidGlassGradients.glassSurface)
                .border(
                    width = 1.dp,
                    color = LiquidGlassColors.Glass.border,
                    shape = RoundedCornerShape(CornerRadius.medium)
                )
                .padding(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            content()
        }
    }
}

/**
 * Divider for separating items within a section
 */
@Composable
fun SettingsDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = LiquidGlassColors.Glass.border.copy(alpha = 0.3f)
    )
}
