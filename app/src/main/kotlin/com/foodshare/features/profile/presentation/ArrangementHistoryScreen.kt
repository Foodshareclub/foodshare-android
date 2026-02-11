package com.foodshare.features.profile.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.core.validation.ValidationBridge
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Arrangement History screen displaying a timeline of past arrangements.
 *
 * Features:
 * - List of arrangements with status badges (pending, accepted, completed, cancelled)
 * - Each item shows counterparty name, listing title, date, and status
 * - Empty state when no arrangements exist
 * - Pull-to-refresh via retry button on error
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrangementHistoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArrangementHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Arrangement History",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = LiquidGlassColors.brandTeal
                        )
                    }
                }

                uiState.error != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = LiquidGlassColors.error,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.height(Spacing.md))

                        Text(
                            text = uiState.error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(Spacing.lg))

                        GlassButton(
                            text = "Retry",
                            onClick = { viewModel.refresh() },
                            style = GlassButtonStyle.Secondary
                        )
                    }
                }

                uiState.isEmpty -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(Modifier.height(Spacing.md))

                        Text(
                            text = "No Arrangements Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(Spacing.xxs))

                        Text(
                            text = "Your arrangement history will appear here once you start sharing or receiving food.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = Spacing.lg)
                        )
                    }
                }

                else -> {
                    // Arrangement list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        item {
                            Spacer(Modifier.height(Spacing.sm))
                        }

                        items(
                            items = uiState.arrangements,
                            key = { it.id }
                        ) { arrangement ->
                            ArrangementHistoryCard(arrangement = arrangement)
                        }

                        item {
                            Spacer(Modifier.height(Spacing.xxl))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a single arrangement history entry.
 */
@Composable
private fun ArrangementHistoryCard(
    arrangement: ArrangementHistoryItem,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            // Top row: listing title and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = arrangement.listingTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(Spacing.xxs))

                StatusBadge(status = arrangement.status)
            }

            Spacer(Modifier.height(Spacing.xxs))

            // Counterparty name
            Text(
                text = "with ${arrangement.counterpartyName}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(Spacing.xs))

            // Date row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )

                Spacer(Modifier.width(Spacing.xxxs))

                Text(
                    text = ValidationBridge.formatDateAndTime(arrangement.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )

                // Show completion date if completed
                if (arrangement.completedAt != null) {
                    Text(
                        text = " - Completed ${ValidationBridge.formatDateShort(arrangement.completedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiquidGlassColors.success.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Status badge composable that displays the arrangement status
 * with an appropriate color and icon.
 */
@Composable
private fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val statusConfig = getStatusConfig(status)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(statusConfig.color.copy(alpha = 0.15f))
            .padding(horizontal = Spacing.xxs, vertical = Spacing.xxxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxxs)
    ) {
        Icon(
            imageVector = statusConfig.icon,
            contentDescription = null,
            tint = statusConfig.color,
            modifier = Modifier.size(12.dp)
        )

        Text(
            text = statusConfig.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = statusConfig.color
        )
    }
}

/**
 * Configuration data for a status badge.
 */
private data class StatusConfig(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

/**
 * Map an arrangement status string to its display configuration.
 */
private fun getStatusConfig(status: String): StatusConfig {
    return when (status.lowercase()) {
        "pending" -> StatusConfig(
            label = "Pending",
            color = LiquidGlassColors.warning,
            icon = Icons.Default.HourglassEmpty
        )
        "accepted" -> StatusConfig(
            label = "Accepted",
            color = LiquidGlassColors.info,
            icon = Icons.Default.ThumbUp
        )
        "completed" -> StatusConfig(
            label = "Completed",
            color = LiquidGlassColors.success,
            icon = Icons.Default.CheckCircle
        )
        "cancelled" -> StatusConfig(
            label = "Cancelled",
            color = LiquidGlassColors.error,
            icon = Icons.Default.Close
        )
        else -> StatusConfig(
            label = status.replaceFirstChar { it.uppercase() },
            color = LiquidGlassColors.accentGray,
            icon = Icons.Default.HourglassEmpty
        )
    }
}
