package com.foodshare.features.settings.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Data Export screen (GDPR compliance)
 *
 * Allows users to request and download their data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Export My Data",
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

            // Explanation
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Download Your Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = LiquidGlassColors.Text.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Request a copy of all your data stored in FoodShare. This includes your profile, listings, messages, and activity history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LiquidGlassColors.Text.secondary
                )

                Text(
                    text = "The export process may take a few minutes. You'll receive a download link when your data is ready.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlassColors.Text.tertiary
                )
            }

            // Status display
            uiState.status?.let { status ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "Export Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = LiquidGlassColors.brandPink,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = when (status) {
                            "pending" -> "Your export is being prepared..."
                            "processing" -> "Processing your data..."
                            "completed" -> "Your data is ready to download!"
                            "failed" -> "Export failed. Please try again."
                            "expired" -> "Download link has expired."
                            else -> "Status: $status"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (status) {
                            "completed" -> LiquidGlassColors.success
                            "failed", "expired" -> LiquidGlassColors.error
                            else -> LiquidGlassColors.Text.secondary
                        }
                    )

                    uiState.expiresAt?.let { expiresAt ->
                        Text(
                            text = "Expires: $expiresAt",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlassColors.Text.tertiary
                        )
                    }
                }
            }

            // Action buttons
            when (uiState.status) {
                "completed" -> {
                    GlassButton(
                        text = "Download Data",
                        onClick = {
                            uiState.downloadUrl?.let { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        },
                        style = GlassButtonStyle.Primary
                    )
                }
                "pending", "processing" -> {
                    GlassButton(
                        text = "Processing...",
                        onClick = { },
                        style = GlassButtonStyle.Secondary,
                        enabled = false,
                        isLoading = uiState.isPolling
                    )
                }
                else -> {
                    GlassButton(
                        text = "Request Data Export",
                        onClick = { viewModel.requestExport() },
                        style = GlassButtonStyle.Primary,
                        isLoading = uiState.isRequesting
                    )
                }
            }

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlassColors.error
                )
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
