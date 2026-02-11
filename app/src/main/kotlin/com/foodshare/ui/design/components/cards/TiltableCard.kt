package com.foodshare.ui.design.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassAnimations

/**
 * Tiltable glass card with pointer/gyro tilt effect
 *
 * Features:
 * - Uses graphicsLayer for GPU-optimized tilt rendering
 * - Animated spring transitions for smooth tilt
 * - Drag gesture support for pointer-based tilt
 * - Wraps content in GlassCard
 * - Auto-resets to neutral position when released
 *
 * Tilt calculation:
 * - rotationX: vertical tilt (up/down)
 * - rotationY: horizontal tilt (left/right)
 * - Max tilt angle: ±10 degrees
 */
@Composable
fun TiltableCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Animate tilt with spring physics
    val animatedRotationX by animateFloatAsState(
        targetValue = offsetY / 30f, // Max ~10 degrees
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "rotationX"
    )

    val animatedRotationY by animateFloatAsState(
        targetValue = -offsetX / 30f, // Max ~10 degrees (inverted)
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "rotationY"
    )

    GlassCard(
        modifier = modifier
            .graphicsLayer {
                rotationX = animatedRotationX
                rotationY = animatedRotationY
                cameraDistance = 12f * density
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Reset to neutral position
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        // Reset to neutral position
                        offsetX = 0f
                        offsetY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x).coerceIn(-300f, 300f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(-300f, 300f)
                }
            },
        cornerRadius = CornerRadius.medium,
        shadow = GlassShadow.Medium,
        content = content
    )
}
