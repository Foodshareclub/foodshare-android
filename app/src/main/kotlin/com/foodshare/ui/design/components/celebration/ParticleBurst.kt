package com.foodshare.ui.design.components.celebration

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.foodshare.ui.design.tokens.LiquidGlassColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Data class representing a burst particle
 */
private data class BurstParticle(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float
)

/**
 * Radial burst animation effect with particles radiating from center
 *
 * @param isPlaying Whether the burst animation is playing
 * @param center The center point of the burst
 * @param modifier Optional modifier for the canvas
 */
@Composable
fun ParticleBurst(
    isPlaying: Boolean,
    center: Offset,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val colors = listOf(
            LiquidGlassColors.brandPink,
            LiquidGlassColors.brandTeal,
            LiquidGlassColors.brandOrange,
            LiquidGlassColors.brandGreen,
            LiquidGlassColors.brandBlue
        )

        List(20) {
            BurstParticle(
                angle = (it * 360f / 20f) + Random.nextFloat() * 18f, // Evenly distributed with slight randomness
                speed = Random.nextFloat() * 100f + 150f, // Speed between 150-250
                color = colors.random(),
                size = Random.nextFloat() * 6f + 3f // Size between 3-9
            )
        }
    }

    val animatable = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            animatable.snapTo(0f)
        }
    }

    val progress = animatable.value

    Canvas(modifier = modifier.fillMaxSize()) {
        if (progress > 0f) {
            particles.forEach { particle ->
                // Calculate position based on angle and progress
                val distance = particle.speed * progress
                val x = center.x + cos(Math.toRadians(particle.angle.toDouble())).toFloat() * distance
                val y = center.y + sin(Math.toRadians(particle.angle.toDouble())).toFloat() * distance

                // Fade out as particles travel
                val alpha = 1f - progress
                val color = particle.color.copy(alpha = alpha)

                drawCircle(
                    color = color,
                    radius = particle.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}
