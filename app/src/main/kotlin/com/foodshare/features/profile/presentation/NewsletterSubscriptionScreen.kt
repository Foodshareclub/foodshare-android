package com.foodshare.features.profile.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.components.inputs.GlassToggle
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Newsletter Subscription screen for managing newsletter preferences.
 *
 * Features:
 * - Toggle for newsletter subscription opt-in/opt-out
 * - Frequency selector (weekly/monthly)
 * - Topics of interest checkboxes
 * - Save button to persist preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsletterSubscriptionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewsletterSubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Newsletter",
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LiquidGlassColors.brandTeal)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md)
            ) {
                Spacer(Modifier.height(Spacing.sm))

                // Newsletter Subscription Toggle
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = LiquidGlassColors.brandPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Subscribe to Newsletter",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(Modifier.height(Spacing.xxxs))

                                Text(
                                    text = "Stay informed about FoodShare news and updates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            GlassToggle(
                                checked = uiState.isSubscribed,
                                onCheckedChange = { viewModel.toggleSubscription(it) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // Frequency Selector (only visible when subscribed)
                if (uiState.isSubscribed) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = "Delivery Frequency",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(Modifier.height(Spacing.xxs))

                            Text(
                                text = "Choose how often you want to receive the newsletter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Spacer(Modifier.height(Spacing.sm))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                NewsletterFrequency.entries.forEach { frequency ->
                                    FrequencyChip(
                                        label = frequency.displayName,
                                        isSelected = uiState.frequency == frequency,
                                        onClick = { viewModel.setFrequency(frequency) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // Topics of Interest
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = "Topics of Interest",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(Modifier.height(Spacing.xxs))

                            Text(
                                text = "Select the topics you are interested in.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Spacer(Modifier.height(Spacing.sm))

                            NewsletterTopic.entries.forEach { topic ->
                                TopicCheckboxRow(
                                    topic = topic,
                                    isChecked = topic in uiState.selectedTopics,
                                    onCheckedChange = { viewModel.toggleTopic(topic) }
                                )

                                if (topic != NewsletterTopic.entries.last()) {
                                    Spacer(Modifier.height(Spacing.xxs))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // Success message
                if (uiState.successMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LiquidGlassColors.success.copy(alpha = 0.15f))
                            .padding(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LiquidGlassColors.success,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = uiState.successMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlassColors.success
                        )
                    }

                    Spacer(Modifier.height(Spacing.xs))
                }

                // Error message
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlassColors.error,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                }

                // Save button
                GlassButton(
                    text = if (uiState.isSaving) "Saving..." else "Save Preferences",
                    onClick = { viewModel.savePreferences() },
                    style = GlassButtonStyle.Primary,
                    isLoading = uiState.isSaving,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }
}

/**
 * Selectable frequency chip for weekly/monthly selection.
 */
@Composable
private fun FrequencyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        LiquidGlassColors.brandTeal.copy(alpha = 0.2f)
    } else {
        LiquidGlassColors.Glass.micro
    }

    val borderColor = if (isSelected) {
        LiquidGlassColors.brandTeal.copy(alpha = 0.6f)
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        LiquidGlassColors.brandTeal
    } else {
        Color.White.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.background(backgroundColor)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs, horizontal = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

/**
 * Checkbox-style row for a newsletter topic.
 */
@Composable
private fun TopicCheckboxRow(
    topic: NewsletterTopic,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isChecked) LiquidGlassColors.brandTeal.copy(alpha = 0.08f)
                else LiquidGlassColors.Glass.micro
            )
            .clickable(onClick = onCheckedChange)
            .padding(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox indicator
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isChecked) LiquidGlassColors.brandTeal
                    else LiquidGlassColors.Glass.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.xs)
        ) {
            Text(
                text = topic.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = topic.description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
