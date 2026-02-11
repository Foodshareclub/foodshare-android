package com.foodshare.features.settings.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
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
import com.foodshare.features.settings.presentation.components.SettingsRow
import com.foodshare.features.settings.presentation.components.SettingsSection
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonSmall
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.components.inputs.GlassPasswordField
import com.foodshare.ui.design.components.inputs.GlassToggle
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Login & Security screen
 *
 * Security hub providing password management, MFA enrollment,
 * biometric setup, and active session management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSecurityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMfaEnrollment: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginSecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Login & Security",
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
                CircularProgressIndicator(color = LiquidGlassColors.brandPink)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Spacer(Modifier.height(Spacing.sm))

                // Success message
                uiState.successMessage?.let { message ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlassColors.success,
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                }

                // Error message
                uiState.error?.let { error ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlassColors.error,
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                }

                // Change Password Section
                SettingsSection(title = "Change Password") {
                    Column(
                        modifier = Modifier.padding(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        GlassPasswordField(
                            value = uiState.currentPassword,
                            onValueChange = viewModel::updateCurrentPassword,
                            label = "Current Password",
                            placeholder = "Enter current password",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassPasswordField(
                            value = uiState.newPassword,
                            onValueChange = viewModel::updateNewPassword,
                            label = "New Password",
                            placeholder = "Enter new password",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassPasswordField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::updateConfirmPassword,
                            label = "Confirm New Password",
                            placeholder = "Re-enter new password",
                            error = uiState.passwordError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(Spacing.xs))

                        GlassButton(
                            text = "Update Password",
                            onClick = viewModel::changePassword,
                            style = GlassButtonStyle.Primary,
                            isLoading = uiState.isChangingPassword,
                            enabled = uiState.currentPassword.isNotBlank()
                                    && uiState.newPassword.isNotBlank()
                                    && uiState.confirmPassword.isNotBlank()
                        )
                    }
                }

                // MFA Enrollment Section
                SettingsSection(title = "Two-Factor Authentication") {
                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = if (uiState.isMfaEnabled) "MFA Enabled" else "Enable MFA",
                        subtitle = if (uiState.isMfaEnabled) {
                            "Your account is protected with two-factor authentication"
                        } else {
                            "Add an extra layer of security to your account"
                        },
                        onClick = onNavigateToMfaEnrollment
                    )
                }

                // Biometric Setup Section
                SettingsSection(title = "Biometric Authentication") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = LiquidGlassColors.brandPink,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Biometric Login",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LiquidGlassColors.Text.primary
                                )
                                Text(
                                    text = "Use fingerprint or face to sign in",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LiquidGlassColors.Text.secondary
                                )
                            }
                        }
                        GlassToggle(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = viewModel::toggleBiometric
                        )
                    }
                }

                // Active Sessions Section
                SettingsSection(title = "Active Sessions") {
                    if (uiState.sessions.isEmpty()) {
                        Text(
                            text = "No active sessions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlassColors.Text.secondary,
                            modifier = Modifier.padding(Spacing.sm)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            uiState.sessions.forEach { session ->
                                SessionRow(
                                    session = session,
                                    onRevoke = { viewModel.revokeSession(session.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }
}

/**
 * Active session row component
 */
@Composable
private fun SessionRow(
    session: ActiveSession,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (session.isCurrent) {
                            LiquidGlassColors.success.copy(alpha = 0.2f)
                        } else {
                            LiquidGlassColors.Glass.background
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = if (session.isCurrent) {
                        LiquidGlassColors.success
                    } else {
                        LiquidGlassColors.Text.secondary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.deviceName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidGlassColors.Text.primary,
                        fontWeight = FontWeight.Medium
                    )
                    if (session.isCurrent) {
                        Text(
                            text = "(this device)",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlassColors.success
                        )
                    }
                }
                Text(
                    text = "Last active: ${session.lastActiveAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlassColors.Text.tertiary
                )
            }
        }

        if (!session.isCurrent) {
            GlassButtonSmall(
                text = "Revoke",
                onClick = onRevoke,
                style = GlassButtonStyle.Destructive
            )
        }
    }
}
