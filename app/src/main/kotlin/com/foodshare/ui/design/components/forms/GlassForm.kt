package com.foodshare.ui.design.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Glassmorphism Form Container with Validation Errors
 *
 * Features:
 * - Glass surface background
 * - Optional title header
 * - Validation error display (red-tinted glass container)
 * - Simple column-based layout
 * - Consistent spacing and borders
 *
 * Ported from iOS form patterns
 */
@Composable
fun GlassForm(
    modifier: Modifier = Modifier,
    title: String? = null,
    errors: List<String> = emptyList(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Optional title
        title?.let {
            Text(
                text = it,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        // Error container (shown only if errors exist)
        if (errors.isNotEmpty()) {
            GlassFormErrorContainer(errors = errors)
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        // Form content
        content()
    }
}

/**
 * Error container with red-tinted glass effect
 */
@Composable
private fun GlassFormErrorContainer(
    errors: List<String>,
    modifier: Modifier = Modifier
) {
    val errorShape = RoundedCornerShape(CornerRadius.medium)

    // Red-tinted glass gradient
    val errorGradient = Brush.verticalGradient(
        colors = listOf(
            LiquidGlassColors.error.copy(alpha = 0.25f),
            LiquidGlassColors.error.copy(alpha = 0.15f)
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(errorShape)
            .background(brush = errorGradient)
            .border(
                width = 1.dp,
                color = LiquidGlassColors.error.copy(alpha = 0.4f),
                shape = errorShape
            )
            .padding(Spacing.sm)
    ) {
        errors.forEachIndexed { index, error ->
            Text(
                text = error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                lineHeight = 20.sp
            )

            // Add spacing between errors
            if (index < errors.size - 1) {
                Spacer(modifier = Modifier.height(Spacing.xxxs))
            }
        }
    }
}
