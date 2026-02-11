package com.foodshare.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.features.forum.domain.model.BadgeRarity
import com.foodshare.features.forum.domain.model.BadgeType
import com.foodshare.features.forum.domain.model.ForumBadge
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Full badges detail screen showing all badges grouped by type.
 *
 * Each badge shows its icon, name, and rarity color. Earned badges
 * have full color, while locked badges are grayed out with a
 * progress indicator.
 *
 * SYNC: This mirrors the iOS BadgesDetailView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BadgesDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Badges",
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
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LiquidGlassColors.brandPink)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = LiquidGlassColors.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                BadgesContent(
                    allBadges = uiState.allBadges,
                    earnedBadgeIds = uiState.earnedBadgeIds,
                    badgeProgress = uiState.badgeProgress,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun BadgesContent(
    allBadges: List<ForumBadge>,
    earnedBadgeIds: Set<Int>,
    badgeProgress: Map<Int, Double>,
    modifier: Modifier = Modifier
) {
    val badgesByType = allBadges
        .groupBy { it.badgeType }
        .toSortedMap(compareBy { it.sortOrder })

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = Spacing.md,
            vertical = Spacing.sm
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Summary card
        item {
            BadgesSummaryCard(
                totalBadges = allBadges.size,
                earnedCount = earnedBadgeIds.size
            )
        }

        // Badge groups by type
        badgesByType.forEach { (type, badges) ->
            item(key = "header-${type.name}") {
                BadgeTypeHeader(type = type, count = badges.size)
            }

            item(key = "grid-${type.name}") {
                BadgeGrid(
                    badges = badges,
                    earnedBadgeIds = earnedBadgeIds,
                    badgeProgress = badgeProgress
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun BadgesSummaryCard(
    totalBadges: Int,
    earnedCount: Int
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$earnedCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiquidGlassColors.brandPink
                )
                Text(
                    text = "Earned",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalBadges",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val percent = if (totalBadges > 0) {
                    (earnedCount * 100) / totalBadges
                } else 0
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiquidGlassColors.success
                )
                Text(
                    text = "Complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun BadgeTypeHeader(
    type: BadgeType,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = badgeTypeIcon(type),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun BadgeGrid(
    badges: List<ForumBadge>,
    earnedBadgeIds: Set<Int>,
    badgeProgress: Map<Int, Double>
) {
    // Use a column with rows of 3 to avoid nested scrollable LazyVerticalGrid
    val rows = badges.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        rows.forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                rowBadges.forEach { badge ->
                    BadgeItem(
                        badge = badge,
                        isEarned = earnedBadgeIds.contains(badge.id),
                        progress = badgeProgress[badge.id] ?: 0.0,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining spots if row is incomplete
                repeat(3 - rowBadges.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: ForumBadge,
    isEarned: Boolean,
    progress: Double,
    modifier: Modifier = Modifier
) {
    val rarityColor = Color(badge.rarity.colorHex)
    val badgeColor = parseBadgeHexColor(badge.resolvedColor)
    val contentAlpha = if (isEarned) 1f else 0.4f

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEarned) badgeColor.copy(alpha = 0.2f)
                        else LiquidGlassColors.Glass.extraSubtle
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = materialIcon(badge.materialIconName),
                    contentDescription = badge.name,
                    tint = if (isEarned) badgeColor else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )

                // Lock overlay for unearned badges
                if (!isEarned) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Badge name
            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Rarity label
            Text(
                text = badge.rarity.displayName,
                fontSize = 9.sp,
                color = rarityColor.copy(alpha = contentAlpha),
                fontWeight = FontWeight.SemiBold
            )

            // Progress bar for unearned badges with auto criteria
            if (!isEarned && badge.hasAutoCriteria && progress > 0.0) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = badgeColor,
                    trackColor = LiquidGlassColors.Glass.background,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

// ========================================================================
// Icon Mapping Helpers
// ========================================================================

private fun badgeTypeIcon(type: BadgeType): ImageVector = when (type) {
    BadgeType.MILESTONE -> Icons.Default.Flag
    BadgeType.ACHIEVEMENT -> Icons.Default.EmojiEvents
    BadgeType.SPECIAL -> Icons.Default.AutoAwesome
}

/**
 * Maps Material icon name strings to ImageVector.
 */
private fun materialIcon(name: String): ImageVector = when (name) {
    "edit" -> Icons.Default.Edit
    "draw" -> Icons.Default.Draw
    "menu_book" -> Icons.Default.MenuBook
    "emoji_events" -> Icons.Default.EmojiEvents
    "chat_bubble" -> Icons.Default.ChatBubble
    "forum" -> Icons.Default.Forum
    "group" -> Icons.Default.Group
    "lightbulb" -> Icons.Default.Lightbulb
    "auto_awesome" -> Icons.Default.AutoAwesome
    "star" -> Icons.Default.Star
    "military_tech" -> Icons.Default.MilitaryTech
    "verified_user" -> Icons.Default.VerifiedUser
    "favorite" -> Icons.Default.Favorite
    "trending_up" -> Icons.Default.TrendingUp
    "schedule" -> Icons.Default.Schedule
    "verified" -> Icons.Default.Verified
    "shield" -> Icons.Default.Shield
    "eco" -> Icons.Default.Eco
    "restaurant" -> Icons.Default.Restaurant
    "help" -> Icons.Default.Help
    "place" -> Icons.Default.Place
    "map" -> Icons.Default.Map
    "home" -> Icons.Default.Home
    "card_giftcard" -> Icons.Default.CardGiftcard
    "photo_camera" -> Icons.Default.PhotoCamera
    "image" -> Icons.Default.Image
    "share" -> Icons.Default.Share
    "bookmark" -> Icons.Default.Bookmark
    "flag" -> Icons.Default.Flag
    "notifications" -> Icons.Default.Notifications
    "settings" -> Icons.Default.Settings
    "search" -> Icons.Default.Search
    "check" -> Icons.Default.Check
    "check_circle" -> Icons.Default.CheckCircle
    "close" -> Icons.Default.Close
    "cancel" -> Icons.Default.Cancel
    "info" -> Icons.Default.Info
    "error" -> Icons.Default.Error
    "warning" -> Icons.Default.Warning
    "bolt" -> Icons.Default.LocalFireDepartment
    "local_fire_department" -> Icons.Default.LocalFireDepartment
    "inventory_2" -> Icons.Default.Inventory2
    "local_shipping" -> Icons.Default.LocalShipping
    "calendar_today" -> Icons.Default.CalendarToday
    "person" -> Icons.Default.Person
    "person_add" -> Icons.Default.PersonAdd
    "how_to_reg" -> Icons.Default.HowToReg
    "thumb_up" -> Icons.Default.ThumbUp
    "thumb_down" -> Icons.Default.ThumbDown
    "sentiment_satisfied" -> Icons.Default.SentimentSatisfied
    "sentiment_dissatisfied" -> Icons.Default.SentimentDissatisfied
    "light_mode" -> Icons.Default.LightMode
    "dark_mode" -> Icons.Default.DarkMode
    else -> Icons.Default.Star
}

/**
 * Parses a hex color string into a Compose Color with fallback.
 */
private fun parseBadgeHexColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorLong = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000 or colorLong)
            8 -> Color(colorLong)
            else -> Color(0xFF22C55E)
        }
    } catch (_: Exception) {
        Color(0xFF22C55E)
    }
}
