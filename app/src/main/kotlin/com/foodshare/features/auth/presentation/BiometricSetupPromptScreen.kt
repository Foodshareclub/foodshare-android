package com.foodshare.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Biometric Setup Prompt Screen
 *
 * Post-login prompt to enable biometric authentication
 *
 * Features:
 * - Shield icon with brand gradient
 * - "Secure Your App" title
 * - Explanation text
 * - "Enable Biometrics" button (Primary)
 * - "Not Now" button (Ghost)
 */
@Composable
fun BiometricSetupPromptScreen(
    onEnable: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LiquidGlassGradients.darkAuth)
    ) {
        // Nature accent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = LiquidGlassGradients.natureAccent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = LiquidGlassGradients.brand,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            // Title
            Text(
                text = "Secure Your App",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.md))

            // Description
            Text(
                text = "Add an extra layer of security by enabling biometric authentication. " +
                        "You'll need to use your fingerprint or face to unlock the app.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.xxl))

            // Enable button
            GlassButton(
                text = "Enable Biometrics",
                onClick = onEnable,
                style = GlassButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Spacing.md))

            // Skip button
            GlassButton(
                text = "Not Now",
                onClick = onSkip,
                style = GlassButtonStyle.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
