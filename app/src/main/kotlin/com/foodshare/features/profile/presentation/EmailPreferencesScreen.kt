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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.components.inputs.GlassToggle
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Email Preferences screen with per-type email notification toggles.
 *
 * Features:
 * - Marketing emails toggle
 * - Product updates toggle
 * - Community notifications toggle
 * - Food alerts toggle
 * - Weekly digest toggle
 * - Each toggle uses a GlassCard row with label and switch
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailPreferencesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmailPreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Email Preferences",
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
                    .padding(horizontal = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Spacer(Modifier.height(Spacing.sm))

                // Header description
                Text(
                    text = "Choose which types of emails you would like to receive from FoodShare.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )

                // Marketing Emails
                EmailPreferenceToggle(
                    title = "Marketing Emails",
                    subtitle = "Promotions, special offers, and partnership highlights",
                    checked = uiState.marketingEnabled,
                    onCheckedChange = viewModel::toggleMarketing
                )

                // Product Updates
                EmailPreferenceToggle(
                    title = "Product Updates",
                    subtitle = "New features, improvements, and app updates",
                    checked = uiState.productUpdatesEnabled,
                    onCheckedChange = viewModel::toggleProductUpdates
                )

                // Community Notifications
                EmailPreferenceToggle(
                    title = "Community Notifications",
                    subtitle = "Activity from your neighbors and community events",
                    checked = uiState.communityNotificationsEnabled,
                    onCheckedChange = viewModel::toggleCommunityNotifications
                )

                // Food Alerts
                EmailPreferenceToggle(
                    title = "Food Alerts",
                    subtitle = "Notifications about new food listings near you",
                    checked = uiState.foodAlertsEnabled,
                    onCheckedChange = viewModel::toggleFoodAlerts
                )

                // Weekly Digest
                EmailPreferenceToggle(
                    title = "Weekly Digest",
                    subtitle = "A weekly summary of activity in your area",
                    checked = uiState.weeklyDigestEnabled,
                    onCheckedChange = viewModel::toggleWeeklyDigest
                )

                Spacer(Modifier.height(Spacing.sm))

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
                }

                // Error message
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlassColors.error,
                        modifier = Modifier.padding(vertical = Spacing.xxxs)
                    )
                }

                // Informational footer
                Text(
                    text = "Changes are saved automatically. You can update your preferences at any time.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = Spacing.xs)
                )

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }
}

/**
 * Reusable composable for a single email preference toggle row.
 *
 * Displays a GlassCard containing a title, subtitle, and a toggle switch.
 *
 * @param title The primary label for the email preference
 * @param subtitle A brief description of what the preference controls
 * @param checked Whether the toggle is currently enabled
 * @param onCheckedChange Callback when the toggle state changes
 */
@Composable
private fun EmailPreferenceToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            GlassToggle(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
