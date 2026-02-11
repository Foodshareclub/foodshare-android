package com.foodshare.ui.design.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Glass chat bubble component with sent and received styles
 *
 * Ported from iOS: GlassChatBubble.swift (pattern)
 *
 * Features:
 * - Sent messages: brand gradient background, right-aligned, rounded except bottom-right
 * - Received messages: glass surface background, left-aligned, rounded except bottom-left
 * - Timestamp in small text with transparency
 * - Follows chat design patterns
 */
@Composable
fun GlassChatBubble(
    message: String,
    isSent: Boolean,
    timestamp: String,
    modifier: Modifier = Modifier
) {
    // Define shape based on sent/received
    val shape = if (isSent) {
        // Sent: rounded corners except bottom-right
        RoundedCornerShape(
            topStart = CornerRadius.medium,
            topEnd = CornerRadius.medium,
            bottomStart = CornerRadius.medium,
            bottomEnd = 4.dp
        )
    } else {
        // Received: rounded corners except bottom-left
        RoundedCornerShape(
            topStart = CornerRadius.medium,
            topEnd = CornerRadius.medium,
            bottomStart = 4.dp,
            bottomEnd = CornerRadius.medium
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth(0.75f)
            .clip(shape)
            .background(
                brush = if (isSent) {
                    LiquidGlassGradients.brand
                } else {
                    LiquidGlassGradients.glassSurface
                }
            )
            .border(
                width = 1.dp,
                color = LiquidGlassColors.Glass.border,
                shape = shape
            )
            .padding(Spacing.sm),
        horizontalAlignment = if (isSent) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // Message text
        Text(
            text = message,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(Spacing.xxxs))

        // Timestamp
        Text(
            text = timestamp,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
