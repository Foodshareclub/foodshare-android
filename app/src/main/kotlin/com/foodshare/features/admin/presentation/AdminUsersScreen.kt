package com.foodshare.features.admin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodshare.features.admin.domain.model.AdminUserProfile
import com.foodshare.features.admin.domain.model.Role
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.components.inputs.GlassTextArea
import com.foodshare.ui.design.components.inputs.GlassTextField
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    uiState: AdminUiState,
    onSearchChange: (String) -> Unit,
    onSelectUser: (AdminUserProfile?) -> Unit,
    onBanUser: (String, String) -> Unit,
    onUnbanUser: (String) -> Unit,
    onAssignRole: (String, Int) -> Unit,
    onRevokeRole: (String, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        GlassTextField(
            value = uiState.userFilters.query,
            onValueChange = onSearchChange,
            placeholder = "Search users...",
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        )

        when {
            uiState.isLoadingUsers -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LiquidGlassColors.brandTeal)
                }
            }

            uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No users found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(
                        items = uiState.users,
                        key = { it.id }
                    ) { user ->
                        UserRow(
                            user = user,
                            onClick = { onSelectUser(user) }
                        )
                    }
                }
            }
        }
    }

    // User detail bottom sheet
    uiState.selectedUser?.let { user ->
        UserDetailSheet(
            user = user,
            roles = uiState.roles,
            onDismiss = { onSelectUser(null) },
            onBan = { reason -> onBanUser(user.id, reason) },
            onUnban = { onUnbanUser(user.id) },
            onAssignRole = { roleId -> onAssignRole(user.id, roleId) },
            onRevokeRole = { roleId -> onRevokeRole(user.id, roleId) }
        )
    }
}

@Composable
private fun UserRow(
    user: AdminUserProfile,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LiquidGlassColors.brandTeal.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.initials,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LiquidGlassColors.brandTeal
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = user.email ?: user.id.take(8),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (user.isBanned) LiquidGlassColors.error.copy(alpha = 0.2f)
                        else LiquidGlassColors.success.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
            ) {
                Text(
                    text = user.statusDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (user.isBanned) LiquidGlassColors.error else LiquidGlassColors.success
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDetailSheet(
    user: AdminUserProfile,
    roles: List<Role>,
    onDismiss: () -> Unit,
    onBan: (String) -> Unit,
    onUnban: () -> Unit,
    onAssignRole: (Int) -> Unit,
    onRevokeRole: (Int) -> Unit
) {
    var banReason by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1A1A2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = user.displayName ?: "Unknown User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = user.email ?: user.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // User info
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    InfoRow("Status", user.statusDisplay)
                    user.createdAt?.let { InfoRow("Joined", it.take(10)) }
                    user.lastSignIn?.let { InfoRow("Last Sign In", it.take(10)) }
                    if (user.isBanned) {
                        user.banReason?.let { InfoRow("Ban Reason", it) }
                    }
                }
            }

            // Roles
            if (roles.isNotEmpty()) {
                Text(
                    text = "Roles",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    roles.forEach { role ->
                        val hasRole = user.roles.any { it.id == role.id }
                        GlassButton(
                            text = role.name,
                            onClick = {
                                if (hasRole) onRevokeRole(role.id) else onAssignRole(role.id)
                            },
                            style = if (hasRole) GlassButtonStyle.Primary else GlassButtonStyle.Secondary
                        )
                    }
                }
            }

            // Ban/Unban actions
            if (user.isBanned) {
                GlassButton(
                    text = "Unban User",
                    onClick = onUnban,
                    icon = Icons.Default.CheckCircle,
                    style = GlassButtonStyle.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Ban User",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = LiquidGlassColors.error
                )
                GlassTextArea(
                    value = banReason,
                    onValueChange = { banReason = it },
                    placeholder = "Reason for ban (min 10 characters)...",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassButton(
                    text = "Ban User",
                    onClick = { onBan(banReason) },
                    enabled = banReason.trim().length >= 10,
                    icon = Icons.Default.Block,
                    style = GlassButtonStyle.Destructive,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
