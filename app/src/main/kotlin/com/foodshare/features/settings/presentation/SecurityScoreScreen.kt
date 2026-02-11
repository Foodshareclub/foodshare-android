package com.foodshare.features.settings.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.core.security.SecurityCheckItem
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Security Score screen
 *
 * Displays user's security score and checklist of security items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScoreScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SecurityScoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Security Score",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        modifier = modifier.background(brush = LiquidGlassGradients.darkAuth)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.md))

            // Circular progress indicator with score
            SecurityScoreIndicator(
                score = uiState.score,
                level = uiState.level.displayName
            )

            // Security checklist
            Text(
                text = "SECURITY CHECKLIST",
                style = MaterialTheme.typography.labelMedium,
                color = LiquidGlassColors.brandPink,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = Spacing.xxs)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                uiState.items.forEach { item ->
                    SecurityCheckItemRow(item = item)
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

/**
 * Circular security score indicator
 */
@Composable
private fun SecurityScoreIndicator(
    score: Int,
    level: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "score_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(180.dp),
                color = LiquidGlassColors.Glass.background,
                strokeWidth = 12.dp,
                trackColor = LiquidGlassColors.Glass.micro,
                strokeCap = StrokeCap.Round
            )

            // Progress circle
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(180.dp),
                color = when {
                    score >= 80 -> LiquidGlassColors.success
                    score >= 60 -> LiquidGlassColors.brandTeal
                    score >= 40 -> LiquidGlassColors.warning
                    else -> LiquidGlassColors.error
                },
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )

            // Score text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge,
                    color = LiquidGlassColors.Text.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp
                )
                Text(
                    text = level,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LiquidGlassColors.Text.secondary
                )
            }
        }
    }
}

/**
 * Security check item row
 */
@Composable
private fun SecurityCheckItemRow(
    item: SecurityCheckItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius.medium))
            .background(brush = LiquidGlassGradients.glassSurface)
            .border(
                width = 1.dp,
                color = LiquidGlassColors.Glass.border,
                shape = RoundedCornerShape(CornerRadius.medium)
            )
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Status icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    color = if (item.isPassed) {
                        LiquidGlassColors.success.copy(alpha = 0.2f)
                    } else {
                        LiquidGlassColors.warning.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.isPassed) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null,
                tint = if (item.isPassed) {
                    LiquidGlassColors.success
                } else {
                    LiquidGlassColors.warning
                },
                modifier = Modifier.size(20.dp)
            )
        }

        // Title and description
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = LiquidGlassColors.Text.primary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlassColors.Text.secondary
            )
        }

        // Points
        Text(
            text = "+${item.points}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.isPassed) {
                LiquidGlassColors.success
            } else {
                LiquidGlassColors.Text.tertiary
            },
            fontWeight = FontWeight.Bold
        )
    }
}
