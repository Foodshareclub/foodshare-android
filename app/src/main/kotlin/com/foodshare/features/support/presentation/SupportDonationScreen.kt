package com.foodshare.features.support.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

private const val KOFI_URL = "https://ko-fi.com/organicnz"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportDonationScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Support Us",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(brush = LiquidGlassGradients.darkAuth)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Spacing.lg))

            // Hero section
            HeroSection()

            Spacer(Modifier.height(Spacing.xl))

            // Ko-fi CTA card
            KofiDonationCard(
                onDonate = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(KOFI_URL))
                    )
                }
            )

            Spacer(Modifier.height(Spacing.xl))

            // Trust indicators
            TrustIndicators()

            Spacer(Modifier.height(Spacing.xl))

            // Impact stats
            ImpactStats()

            Spacer(Modifier.height(Spacing.xl))

            // Mission quote
            MissionQuote()

            Spacer(Modifier.height(Spacing.xl))

            // Final CTA
            FinalCTA(
                onDonate = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(KOFI_URL))
                    )
                },
                onLearnMore = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://foodshare.club/about"))
                    )
                }
            )

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LiquidGlassColors.brandPink,
                            LiquidGlassColors.brandTeal
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = "Help Us Feed\nMore People",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = "Your generosity helps keep Foodshare free and accessible for everyone in our community.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun KofiDonationCard(onDonate: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "kofi_gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kofi_offset"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF5E5B).copy(alpha = 0.15f),
                            Color(0xFFFF9966).copy(alpha = 0.1f)
                        ),
                        start = Offset(0f, animatedOffset * 100f),
                        end = Offset(300f, 300f + animatedOffset * 100f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = Color(0xFFFF5E5B),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(Modifier.height(Spacing.md))

                Text(
                    text = "Buy Us a Coffee",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(Spacing.xs))

                Text(
                    text = "Every contribution, no matter the size, helps us maintain and improve the platform.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(Spacing.lg))

                GlassButton(
                    text = "Donate on Ko-fi",
                    onClick = onDonate,
                    icon = Icons.Default.Favorite,
                    style = GlassButtonStyle.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TrustIndicators() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            tint = LiquidGlassColors.success,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = "Secure payments via Ko-fi",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(Spacing.md))
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = LiquidGlassColors.success,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = "100% goes to Foodshare",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ImpactStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        ImpactCard(
            value = "100%",
            label = "Direct",
            description = "All donations support Foodshare directly",
            modifier = Modifier.weight(1f)
        )
        ImpactCard(
            value = "1000+",
            label = "Lives",
            description = "People helped through food sharing",
            modifier = Modifier.weight(1f)
        )
        ImpactCard(
            value = "Zero",
            label = "Fees",
            description = "No platform fees on donations",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ImpactCard(
    value: String,
    label: String,
    description: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LiquidGlassColors.brandTeal
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(Spacing.xxs))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MissionQuote() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\"",
                style = MaterialTheme.typography.displaySmall,
                color = LiquidGlassColors.brandTeal,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No one has ever become poor by giving. Together, we can reduce food waste and feed our communities.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "- The Foodshare Team",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun FinalCTA(
    onDonate: () -> Unit,
    onLearnMore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        GlassButton(
            text = "Support Foodshare",
            onClick = onDonate,
            icon = Icons.Default.Favorite,
            style = GlassButtonStyle.Primary,
            modifier = Modifier.fillMaxWidth()
        )
        GlassButton(
            text = "Learn More About Us",
            onClick = onLearnMore,
            icon = Icons.AutoMirrored.Filled.OpenInNew,
            style = GlassButtonStyle.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
