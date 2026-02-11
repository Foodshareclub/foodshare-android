package com.foodshare.features.notifications.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

// ============================================================================
// NotificationDropdown - Main Composable
// ============================================================================

/**
 * Notification dropdown component anchored to a bell icon.
 *
 * Shows the last 5 notifications with unread indicators, a "Mark all
 * as read" button, and a "View All" button for navigation to the
 * full notifications screen.
 *
 * Features:
 * - Popup anchored to bell icon position
 * - Unread count badge on bell icon
 * - Blue dot unread indicator per notification
 * - Icon, title, message preview, time ago per item
 * - Mark all as read button
 * - View All button at bottom
 * - Real-time updates
 *
 * SYNC: Mirrors Swift NotificationDropdown
 */
@Composable
fun NotificationDropdown(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
    onMarkAllRead: () -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationDropdownViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        if (isExpanded) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = onDismiss,
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(200)) + scaleIn(
                        animationSpec = tween(200),
                        transformOrigin = TransformOrigin(0.9f, 0f)
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + scaleOut(
                        animationSpec = tween(150),
                        transformOrigin = TransformOrigin(0.9f, 0f)
                    )
                ) {
                    NotificationDropdownContent(
                        uiState = uiState,
                        onNotificationClick = { notification ->
                            viewModel.markAsRead(notification)
                            onNotificationClick(notification)
                            onDismiss()
                        },
                        onMarkAllRead = {
                            viewModel.markAllAsRead()
                            onMarkAllRead()
                        },
                        onViewAll = {
                            onViewAll()
                            onDismiss()
                        },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

// ============================================================================
// Bell Icon with Badge
// ============================================================================

/**
 * Bell icon button with unread count badge.
 * Use this as the anchor for the NotificationDropdown.
 */
@Composable
fun NotificationBellIcon(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = LiquidGlassColors.brandPink,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// Dropdown Content
// ============================================================================

@Composable
private fun NotificationDropdownContent(
    uiState: NotificationDropdownViewModel.UiState,
    onNotificationClick: (NotificationItem) -> Unit,
    onMarkAllRead: () -> Unit,
    onViewAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val shape = RoundedCornerShape(CornerRadius.large)

    Box(
        modifier = Modifier
            .widthIn(min = 300.dp, max = 360.dp)
            .padding(end = Spacing.sm, top = Spacing.xxxs)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A2332),
                        Color(0xFF0D141F)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.sm)
        ) {
            // Header
            DropdownHeader(
                unreadCount = uiState.unreadCount,
                onMarkAllRead = onMarkAllRead,
                onDismiss = onDismiss
            )

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = Spacing.xxxs)
            )

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = LiquidGlassColors.brandTeal,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                uiState.notifications.isEmpty() -> {
                    EmptyDropdownState()
                }

                else -> {
                    // Notification items
                    uiState.notifications.forEachIndexed { index, notification ->
                        DropdownNotificationItem(
                            notification = notification,
                            onClick = { onNotificationClick(notification) }
                        )

                        if (index < uiState.notifications.lastIndex) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.padding(horizontal = Spacing.md)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = Spacing.xxxs)
            )

            // View All button
            DropdownFooter(onViewAll = onViewAll)
        }
    }
}

// ============================================================================
// Header
// ============================================================================

@Composable
private fun DropdownHeader(
    unreadCount: Int,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xxxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (unreadCount > 0) {
                Spacer(modifier = Modifier.width(Spacing.sm))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LiquidGlassColors.brandPink)
                        .padding(horizontal = Spacing.sm, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (unreadCount > 0) {
                TextButton(
                    onClick = onMarkAllRead,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = Spacing.sm,
                        vertical = Spacing.xxxs
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = LiquidGlassColors.brandTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xxxs))
                    Text(
                        text = "Read All",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiquidGlassColors.brandTeal
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ============================================================================
// Notification Item
// ============================================================================

@Composable
private fun DropdownNotificationItem(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                color = if (!notification.isRead)
                    Color.White.copy(alpha = 0.03f)
                else
                    Color.Transparent
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        // Type icon with colored background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(getTypeColor(notification.type).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getTypeIcon(notification.type),
                contentDescription = null,
                tint = getTypeColor(notification.type),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.sm))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Spacing.xxxs))
                Text(
                    text = notification.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            if (notification.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }

        // Unread indicator (blue dot)
        if (!notification.isRead) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                LiquidGlassColors.brandBlue,
                                LiquidGlassColors.brandTeal
                            )
                        )
                    )
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

// ============================================================================
// Empty State
// ============================================================================

@Composable
private fun EmptyDropdownState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// ============================================================================
// Footer
// ============================================================================

@Composable
private fun DropdownFooter(
    onViewAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewAll)
            .padding(vertical = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "View All Notifications",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = LiquidGlassColors.brandTeal
        )
    }
}

// ============================================================================
// Convenience: Full Bell + Dropdown Composable
// ============================================================================

/**
 * Combined bell icon + dropdown in a single composable for easy integration.
 *
 * Usage:
 * ```
 * NotificationBellWithDropdown(
 *     onNotificationClick = { notification -> ... },
 *     onViewAll = { navController.navigate("notifications") }
 * )
 * ```
 */
@Composable
fun NotificationBellWithDropdown(
    onNotificationClick: (NotificationItem) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationDropdownViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isExpanded = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(false)
    }

    Box(modifier = modifier) {
        NotificationBellIcon(
            unreadCount = uiState.unreadCount,
            onClick = {
                isExpanded.value = !isExpanded.value
                if (isExpanded.value) {
                    viewModel.loadRecentNotifications()
                }
            }
        )

        NotificationDropdown(
            isExpanded = isExpanded.value,
            onDismiss = { isExpanded.value = false },
            onNotificationClick = onNotificationClick,
            onMarkAllRead = { /* Already handled by viewModel inside dropdown */ },
            onViewAll = onViewAll,
            viewModel = viewModel
        )
    }
}

// ============================================================================
// Utilities
// ============================================================================

/**
 * Get the icon for a notification type category.
 */
private fun getTypeIcon(type: String): ImageVector = when (type) {
    "listing" -> Icons.Default.Restaurant
    "message" -> Icons.Default.Message
    "forum" -> Icons.Default.Forum
    "challenge" -> Icons.Default.EmojiEvents
    "system" -> Icons.Default.Settings
    else -> Icons.Default.Notifications
}

/**
 * Get the accent color for a notification type category.
 */
private fun getTypeColor(type: String): Color = when (type) {
    "listing" -> LiquidGlassColors.brandGreen
    "message" -> LiquidGlassColors.brandBlue
    "forum" -> LiquidGlassColors.brandTeal
    "challenge" -> LiquidGlassColors.medalGold
    "system" -> LiquidGlassColors.accentGray
    else -> LiquidGlassColors.brandPink
}
