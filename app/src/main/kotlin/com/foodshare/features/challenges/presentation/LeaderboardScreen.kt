package com.foodshare.features.challenges.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

// ============================================================================
// LeaderboardScreen
// ============================================================================

/**
 * Full leaderboard screen with podium view for top 3 users
 * and a scrollable ranked list below.
 *
 * Features:
 * - Podium display: 2nd (left), 1st (center, tallest), 3rd (right)
 * - Medal colors (gold, silver, bronze)
 * - Time period filter chips (This Week, This Month, All Time)
 * - Category filter chips (Food Shared, Community Impact, Challenges Won)
 * - Current user rank highlighted
 * - EngagementBridge-based score display
 *
 * SYNC: Mirrors Swift LeaderboardView
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Leaderboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.entries.isEmpty() && uiState.topThree.isEmpty() -> {
                    LeaderboardLoadingState()
                }

                uiState.error != null && uiState.entries.isEmpty() && uiState.topThree.isEmpty() -> {
                    LeaderboardErrorState(
                        message = uiState.error ?: "Something went wrong",
                        onRetry = { viewModel.loadLeaderboard() }
                    )
                }

                else -> {
                    LeaderboardContent(
                        uiState = uiState,
                        onPeriodSelected = { viewModel.selectPeriod(it) },
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// Content
// ============================================================================

@Composable
private fun LeaderboardContent(
    uiState: LeaderboardViewModel.UiState,
    onPeriodSelected: (LeaderboardViewModel.TimePeriod) -> Unit,
    onCategorySelected: (LeaderboardViewModel.LeaderboardCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxl)
    ) {
        // Time period filter chips
        item {
            TimePeriodFilterRow(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )
        }

        // Category filter chips
        item {
            CategoryFilterRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )
        }

        // Podium for top 3
        item {
            Spacer(modifier = Modifier.height(Spacing.md))
            if (uiState.topThree.isNotEmpty()) {
                PodiumSection(topThree = uiState.topThree)
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // Current user rank highlight (if not in top 3)
        uiState.currentUserEntry?.let { entry ->
            if (entry.rank > 3) {
                item {
                    CurrentUserRankCard(
                        entry = entry,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                    )
                }
            }
        }

        // Ranked list (4th place and below)
        if (uiState.entries.isNotEmpty()) {
            item {
                Text(
                    text = "Rankings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.sm
                    )
                )
            }

            items(
                items = uiState.entries,
                key = { it.userId }
            ) { entry ->
                LeaderboardListItem(
                    entry = entry,
                    modifier = Modifier.padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.xxxs
                    )
                )
            }
        }

        // Loading indicator for refresh
        if (uiState.isRefreshing) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = LiquidGlassColors.brandTeal,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

// ============================================================================
// Filter Rows
// ============================================================================

@Composable
private fun TimePeriodFilterRow(
    selectedPeriod: LeaderboardViewModel.TimePeriod,
    onPeriodSelected: (LeaderboardViewModel.TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(LeaderboardViewModel.TimePeriod.entries) { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = period.displayName,
                        color = if (selectedPeriod == period) Color.White
                        else Color.White.copy(alpha = 0.7f)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LiquidGlassColors.brandPink.copy(alpha = 0.8f),
                    containerColor = Color.White.copy(alpha = 0.1f),
                    selectedLabelColor = Color.White,
                    labelColor = Color.White.copy(alpha = 0.7f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.White.copy(alpha = 0.2f),
                    selectedBorderColor = LiquidGlassColors.brandPink,
                    enabled = true,
                    selected = selectedPeriod == period
                )
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: LeaderboardViewModel.LeaderboardCategory,
    onCategorySelected: (LeaderboardViewModel.LeaderboardCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(LeaderboardViewModel.LeaderboardCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.displayName,
                        color = if (selectedCategory == category) Color.White
                        else Color.White.copy(alpha = 0.7f)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LiquidGlassColors.brandTeal.copy(alpha = 0.8f),
                    containerColor = Color.White.copy(alpha = 0.1f),
                    selectedLabelColor = Color.White,
                    labelColor = Color.White.copy(alpha = 0.7f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.White.copy(alpha = 0.2f),
                    selectedBorderColor = LiquidGlassColors.brandTeal,
                    enabled = true,
                    selected = selectedCategory == category
                )
            )
        }
    }
}

// ============================================================================
// Podium Section
// ============================================================================

/**
 * Podium view showing the top 3 users:
 * - 2nd place on the left (slightly lower)
 * - 1st place in the center (tallest)
 * - 3rd place on the right (lowest)
 */
@Composable
private fun PodiumSection(
    topThree: List<LeaderboardEntry>,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(topThree) { visible = true }

    val first = topThree.getOrNull(0)
    val second = topThree.getOrNull(1)
    val third = topThree.getOrNull(2)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
            animationSpec = tween(600),
            initialOffsetY = { it / 4 }
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2nd place - Silver (left, medium height)
            second?.let {
                PodiumItem(
                    entry = it,
                    podiumHeight = 100.dp,
                    avatarSize = 64.dp,
                    medalColor = LiquidGlassColors.medalSilver,
                    medalGradient = Brush.linearGradient(
                        listOf(
                            LiquidGlassColors.medalSilver,
                            LiquidGlassColors.medalSilver.copy(alpha = 0.6f)
                        )
                    ),
                    medalEmoji = "2",
                    modifier = Modifier.weight(1f)
                )
            } ?: Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.width(Spacing.sm))

            // 1st place - Gold (center, tallest)
            first?.let {
                PodiumItem(
                    entry = it,
                    podiumHeight = 140.dp,
                    avatarSize = 80.dp,
                    medalColor = LiquidGlassColors.medalGold,
                    medalGradient = Brush.linearGradient(
                        listOf(
                            LiquidGlassColors.medalGold,
                            Color(0xFFFFA500) // Orange accent
                        )
                    ),
                    medalEmoji = "1",
                    isFirst = true,
                    modifier = Modifier.weight(1.2f)
                )
            } ?: Spacer(modifier = Modifier.weight(1.2f))

            Spacer(modifier = Modifier.width(Spacing.sm))

            // 3rd place - Bronze (right, shortest)
            third?.let {
                PodiumItem(
                    entry = it,
                    podiumHeight = 80.dp,
                    avatarSize = 56.dp,
                    medalColor = LiquidGlassColors.medalBronze,
                    medalGradient = Brush.linearGradient(
                        listOf(
                            LiquidGlassColors.medalBronze,
                            LiquidGlassColors.medalBronze.copy(alpha = 0.6f)
                        )
                    ),
                    medalEmoji = "3",
                    modifier = Modifier.weight(1f)
                )
            } ?: Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * A single podium item showing avatar, name, score, and podium bar.
 */
@Composable
private fun PodiumItem(
    entry: LeaderboardEntry,
    podiumHeight: androidx.compose.ui.unit.Dp,
    avatarSize: androidx.compose.ui.unit.Dp,
    medalColor: Color,
    medalGradient: Brush,
    medalEmoji: String,
    isFirst: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crown icon for 1st place
        if (isFirst) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Champion",
                tint = LiquidGlassColors.medalGold,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.xxxs))
        }

        // Avatar with medal border
        Box(contentAlignment = Alignment.Center) {
            // Glow ring
            Box(
                modifier = Modifier
                    .size(avatarSize + 8.dp)
                    .clip(CircleShape)
                    .background(medalColor.copy(alpha = 0.3f))
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = medalGradient,
                        shape = CircleShape
                    )
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (entry.avatarUrl != null) {
                    AsyncImage(
                        model = entry.avatarUrl,
                        contentDescription = "${entry.displayName}'s avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(avatarSize * 0.5f)
                    )
                }
            }

            // Medal badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush = medalGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = medalEmoji,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Name
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Score
        Text(
            text = formatScore(entry.score),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = medalColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Podium bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = CornerRadius.medium, topEnd = CornerRadius.medium))
                .background(brush = medalGradient.let {
                    Brush.verticalGradient(
                        listOf(
                            medalColor.copy(alpha = 0.6f),
                            medalColor.copy(alpha = 0.2f)
                        )
                    )
                })
                .border(
                    width = 1.dp,
                    color = medalColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(topStart = CornerRadius.medium, topEnd = CornerRadius.medium)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Rank number on the podium
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

// ============================================================================
// Current User Rank Card
// ============================================================================

/**
 * Highlighted card showing the current user's rank when they are
 * not in the top 3.
 */
@Composable
private fun CurrentUserRankCard(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            LiquidGlassColors.brandPink.copy(alpha = 0.15f),
                            LiquidGlassColors.brandTeal.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                LiquidGlassColors.brandPink,
                                LiquidGlassColors.brandTeal
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = LiquidGlassGradients.brand,
                        shape = CircleShape
                    )
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (entry.avatarUrl != null) {
                    AsyncImage(
                        model = entry.avatarUrl,
                        contentDescription = "Your avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Name & label
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Your Ranking",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Score
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatScore(entry.score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiquidGlassColors.brandPink
                )
                Text(
                    text = "pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ============================================================================
// Leaderboard List Item
// ============================================================================

/**
 * Single row in the ranked list (4th place and below).
 */
@Composable
private fun LeaderboardListItem(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val highlightAlpha = if (entry.isCurrentUser) 0.15f else 0f

    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (entry.isCurrentUser)
                        LiquidGlassColors.brandPink.copy(alpha = highlightAlpha)
                    else
                        Color.Transparent
                )
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (entry.isCurrentUser)
                    LiquidGlassColors.brandPink
                else
                    Color.White.copy(alpha = 0.7f),
                modifier = Modifier.width(40.dp)
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (entry.isCurrentUser) 2.dp else 1.dp,
                        color = if (entry.isCurrentUser)
                            LiquidGlassColors.brandPink
                        else
                            Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (entry.avatarUrl != null) {
                    AsyncImage(
                        model = entry.avatarUrl,
                        contentDescription = "${entry.displayName}'s avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Name
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (entry.isCurrentUser)
                            FontWeight.Bold else FontWeight.Normal,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.isCurrentUser) {
                        Spacer(modifier = Modifier.width(Spacing.xxxs))
                        Text(
                            text = "(You)",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlassColors.brandPink
                        )
                    }
                }
            }

            // Score
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = LiquidGlassColors.medalGold.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xxxs))
                Text(
                    text = formatScore(entry.score),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ============================================================================
// States
// ============================================================================

@Composable
private fun LeaderboardLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = LiquidGlassColors.brandTeal,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = "Loading leaderboard...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun LeaderboardErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        GlassCard {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                com.foodshare.ui.design.components.buttons.GlassButton(
                    text = "Try Again",
                    onClick = onRetry,
                    style = com.foodshare.ui.design.components.buttons.GlassButtonStyle.Primary
                )
            }
        }
    }
}

// ============================================================================
// Utilities
// ============================================================================

/**
 * Format a numeric score for display with K suffix for large numbers.
 */
private fun formatScore(score: Int): String = when {
    score >= 10_000 -> "${score / 1000}K"
    score >= 1_000 -> "%.1fK".format(score / 1000.0)
    else -> score.toString()
}
