package com.foodshare.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Two-Factor Authentication screen
 *
 * Allows users to enable/disable 2FA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorAuthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TwoFactorAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Two-Factor Authentication",
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

            if (uiState.isEnabled) {
                // MFA is enabled
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Two-Factor Authentication is Enabled",
                        style = MaterialTheme.typography.titleMedium,
                        color = LiquidGlassColors.Text.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your account is protected with two-factor authentication. You'll need to enter a code from your authenticator app when signing in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidGlassColors.Text.secondary
                    )

                    Text(
                        text = "Enrolled Factors: ${uiState.enrolledFactors}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlassColors.Text.tertiary
                    )

                    Spacer(Modifier.height(Spacing.md))

                    GlassButton(
                        text = "Disable Two-Factor Auth",
                        onClick = { viewModel.unenrollMFA() },
                        style = GlassButtonStyle.Destructive,
                        isLoading = uiState.isUnenrolling
                    )
                }
            } else {
                // MFA is not enabled
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Secure Your Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = LiquidGlassColors.Text.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Two-factor authentication adds an extra layer of security to your account. You'll need to enter a code from your authenticator app when signing in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidGlassColors.Text.secondary
                    )

                    Text(
                        text = "How it works:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidGlassColors.Text.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        BulletPoint("Download an authenticator app like Google Authenticator or Authy")
                        BulletPoint("Scan the QR code or enter the secret key")
                        BulletPoint("Enter the 6-digit code to verify")
                        BulletPoint("Use codes from your app when signing in")
                    }

                    Spacer(Modifier.height(Spacing.md))

                    GlassButton(
                        text = "Enable Two-Factor Auth",
                        onClick = { viewModel.enrollMFA() },
                        style = GlassButtonStyle.Primary,
                        isLoading = uiState.isEnrolling
                    )
                }
            }

            // QR Code and verification would be shown in a dialog
            // For now, this is a placeholder for the enrollment flow

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

/**
 * Bullet point text
 */
@Composable
private fun BulletPoint(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall,
        color = LiquidGlassColors.Text.secondary,
        modifier = modifier.padding(start = Spacing.sm)
    )
}
