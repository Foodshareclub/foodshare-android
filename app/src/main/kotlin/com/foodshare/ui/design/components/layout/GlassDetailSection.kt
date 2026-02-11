package com.foodshare.ui.design.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.foodshare.ui.design.tokens.LiquidGlassTypography
import com.foodshare.ui.design.tokens.Spacing

/**
 * A labeled content section with glass styling
 *
 * Features:
 * - Label text with labelMedium typography
 * - White text at 0.7f alpha for subtle appearance
 * - Content in padded Column below the label
 */
@Composable
fun GlassDetailSection(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Label text
        Text(
            text = label,
            style = LiquidGlassTypography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = Spacing.xxs)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xxxs)
        ) {
            content()
        }
    }
}
