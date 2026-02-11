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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.foodshare.features.admin.domain.model.ModerationPriority
import com.foodshare.features.admin.domain.model.ModerationQueueItem
import com.foodshare.features.admin.domain.model.ModerationResolution
import com.foodshare.features.admin.domain.model.ModerationStatus
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.components.inputs.GlassTextArea
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModerationScreen(
    uiState: AdminUiState,
    onFilterChange: (ModerationStatus?) -> Unit,
    onSelectItem: (ModerationQueueItem?) -> Unit,
    onResolve: (Int, ModerationResolution, String) -> Unit,
    onDeletePost: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            item {
                FilterChip(
                    selected = uiState.moderationStatusFilter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LiquidGlassColors.brandTeal.copy(alpha = 0.3f),
                        selectedLabelColor = Color.White,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
            items(ModerationStatus.entries) { status ->
                FilterChip(
                    selected = uiState.moderationStatusFilter == status,
                    onClick = { onFilterChange(status) },
                    label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LiquidGlassColors.brandTeal.copy(alpha = 0.3f),
                        selectedLabelColor = Color.White,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        when {
            uiState.isLoadingModeration -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LiquidGlassColors.brandTeal)
                }
            }

            uiState.moderationQueue.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = "No items in queue",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(
                        items = uiState.moderationQueue,
                        key = { it.id }
                    ) { item ->
                        ModerationRow(
                            item = item,
                            onClick = { onSelectItem(item) }
                        )
                    }
                }
            }
        }
    }

    // Resolution bottom sheet
    uiState.selectedModerationItem?.let { item ->
        ResolutionSheet(
            item = item,
            onDismiss = { onSelectItem(null) },
            onResolve = { resolution, notes -> onResolve(item.id, resolution, notes) },
            onDeletePost = { onDeletePost(item.contentId) }
        )
    }
}

@Composable
private fun ModerationRow(
    item: ModerationQueueItem,
    onClick: () -> Unit
) {
    val priorityColor = when (item.priorityEnum) {
        ModerationPriority.CRITICAL -> LiquidGlassColors.error
        ModerationPriority.HIGH -> Color(0xFFFF9800)
        ModerationPriority.MEDIUM -> LiquidGlassColors.warning
        ModerationPriority.LOW -> Color.White.copy(alpha = 0.5f)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(Spacing.md)) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.contentType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(priorityColor.copy(alpha = 0.2f))
                            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                    ) {
                        Text(
                            text = item.priority.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item.reason?.let { reason ->
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                item.contentPreview?.let { preview ->
                    Spacer(Modifier.height(Spacing.xxs))
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 2
                    )
                }

                Spacer(Modifier.height(Spacing.xxs))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    item.reporterName?.let { name ->
                        Text(
                            text = "by $name",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                    item.createdAt?.let { date ->
                        Text(
                            text = date.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResolutionSheet(
    item: ModerationQueueItem,
    onDismiss: () -> Unit,
    onResolve: (ModerationResolution, String) -> Unit,
    onDeletePost: () -> Unit
) {
    var selectedResolution by remember { mutableStateOf<ModerationResolution?>(null) }
    var notes by remember { mutableStateOf("") }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resolve Report",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Report details
            item.reason?.let { reason ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "Reason: $reason",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        item.description?.let { desc ->
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Resolution options
            Text(
                text = "Resolution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            ModerationResolution.entries.forEach { resolution ->
                val isSelected = selectedResolution == resolution
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedResolution = resolution }
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (isSelected) LiquidGlassColors.brandTeal
                                    else LiquidGlassColors.Glass.border
                                )
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = resolution.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // Notes
            GlassTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                placeholder = "Resolution notes (min 5 characters)...",
                modifier = Modifier.fillMaxWidth()
            )

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                GlassButton(
                    text = "Delete Content",
                    onClick = onDeletePost,
                    icon = Icons.Default.Delete,
                    style = GlassButtonStyle.Destructive,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Resolve",
                    onClick = {
                        selectedResolution?.let { resolution ->
                            onResolve(resolution, notes)
                        }
                    },
                    enabled = selectedResolution != null && notes.trim().length >= 5,
                    icon = Icons.Default.Gavel,
                    style = GlassButtonStyle.Primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Spacing.lg))
        }
    }
}
