package com.foodshare.features.settings.presentation

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.foodshare.features.settings.presentation.components.SettingsRow
import com.foodshare.features.settings.presentation.components.SettingsSection
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing
import com.foodshare.ui.theme.ThemePicker

/**
 * Settings screen with full settings menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToSecurityScore: () -> Unit = {},
    onNavigateToBlockedUsers: () -> Unit = {},
    onNavigateToTwoFactorAuth: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToDataExport: () -> Unit = {},
    onNavigateToLegalDocument: (String) -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToSupportDonation: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToLoginSecurity: () -> Unit = {},
    onNavigateToAccessibility: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToAccountDeletion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
        SettingsContent(
            modifier = Modifier.padding(padding),
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToPrivacy = onNavigateToPrivacy,
            onNavigateToSecurityScore = onNavigateToSecurityScore,
            onNavigateToBlockedUsers = onNavigateToBlockedUsers,
            onNavigateToTwoFactorAuth = onNavigateToTwoFactorAuth,
            onNavigateToLanguage = onNavigateToLanguage,
            onNavigateToDataExport = onNavigateToDataExport,
            onNavigateToLegalDocument = onNavigateToLegalDocument,
            onNavigateToFeedback = onNavigateToFeedback,
            onNavigateToSupportDonation = onNavigateToSupportDonation,
            onNavigateToSubscription = onNavigateToSubscription,
            onNavigateToHelp = onNavigateToHelp,
            onNavigateToLoginSecurity = onNavigateToLoginSecurity,
            onNavigateToAccessibility = onNavigateToAccessibility,
            onNavigateToBackup = onNavigateToBackup,
            onNavigateToAccountDeletion = onNavigateToAccountDeletion
        )
    }
}

/**
 * Settings content with all sections
 */
@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToSecurityScore: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToTwoFactorAuth: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToDataExport: () -> Unit,
    onNavigateToLegalDocument: (String) -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToSupportDonation: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToLoginSecurity: () -> Unit = {},
    onNavigateToAccessibility: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToAccountDeletion: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Spacer(Modifier.height(Spacing.md))

        // Account Section
        SettingsSection(title = "Account") {
            SettingsRow(
                icon = Icons.Default.AccountCircle,
                title = "Edit Profile",
                subtitle = "Update your profile information",
                onClick = { /* Navigation handled by parent */ }
            )
            SettingsRow(
                icon = Icons.Default.VpnKey,
                title = "Login & Security",
                subtitle = "Password, MFA, biometrics, sessions",
                onClick = onNavigateToLoginSecurity
            )
            SettingsRow(
                icon = Icons.Default.Security,
                title = "Security Score",
                subtitle = "Check your account security",
                onClick = onNavigateToSecurityScore
            )
        }

        // Notifications Section
        SettingsSection(title = "Notifications") {
            SettingsRow(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Manage notification preferences",
                onClick = onNavigateToNotifications
            )
        }

        // Privacy Section
        SettingsSection(title = "Privacy & Security") {
            SettingsRow(
                icon = Icons.Default.Lock,
                title = "Privacy",
                subtitle = "Control who can see your data",
                onClick = onNavigateToPrivacy
            )
            SettingsRow(
                icon = Icons.Default.Block,
                title = "Blocked Users",
                subtitle = "Manage blocked users",
                onClick = onNavigateToBlockedUsers
            )
            SettingsRow(
                icon = Icons.Default.Shield,
                title = "Two-Factor Authentication",
                subtitle = "Add an extra layer of security",
                onClick = onNavigateToTwoFactorAuth
            )
        }

        // Appearance Section
        SettingsSection(title = "Appearance") {
            // Theme Picker embedded
            Column(modifier = Modifier.padding(Spacing.xs)) {
                ThemePicker()
            }
            SettingsRow(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = "Choose your preferred language",
                onClick = onNavigateToLanguage
            )
            SettingsRow(
                icon = Icons.Default.Accessibility,
                title = "Accessibility",
                subtitle = "Text size, motion, contrast",
                onClick = onNavigateToAccessibility
            )
        }

        // Data Section
        SettingsSection(title = "Data") {
            SettingsRow(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                subtitle = "Export or import your settings",
                onClick = onNavigateToBackup
            )
        }

        // Premium & Support Section
        SettingsSection(title = "Premium & Support") {
            SettingsRow(
                icon = Icons.Default.Diamond,
                title = "Upgrade to Premium",
                subtitle = "Ad-free experience and more",
                onClick = onNavigateToSubscription
            )
            SettingsRow(
                icon = Icons.Default.Feedback,
                title = "Send Feedback",
                subtitle = "Help us improve Foodshare",
                onClick = onNavigateToFeedback
            )
            SettingsRow(
                icon = Icons.Default.VolunteerActivism,
                title = "Support Us",
                subtitle = "Help keep Foodshare running",
                onClick = onNavigateToSupportDonation
            )
            SettingsRow(
                icon = Icons.Default.HelpOutline,
                title = "Help Center",
                subtitle = "FAQs and support",
                onClick = onNavigateToHelp
            )
        }

        // Legal Section
        SettingsSection(title = "Legal") {
            SettingsRow(
                icon = Icons.Default.Description,
                title = "Terms of Service",
                onClick = { onNavigateToLegalDocument("terms") }
            )
            SettingsRow(
                icon = Icons.Default.Policy,
                title = "Privacy Policy",
                onClick = { onNavigateToLegalDocument("privacy") }
            )
            SettingsRow(
                icon = Icons.Default.Download,
                title = "Export My Data",
                subtitle = "Download all your data (GDPR)",
                onClick = onNavigateToDataExport
            )
            SettingsRow(
                icon = Icons.Default.Info,
                title = "Open Source Licenses",
                onClick = { onNavigateToLegalDocument("licenses") }
            )
        }

        // About Section
        SettingsSection(title = "About") {
            SettingsRow(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = "3.0.2 (273)"
            )
        }

        // Danger Zone
        SettingsSection(title = "Danger Zone") {
            GlassButton(
                text = "Delete Account",
                onClick = onNavigateToAccountDeletion,
                style = GlassButtonStyle.Destructive
            )
        }

        Spacer(Modifier.height(Spacing.xxl))
    }
}
