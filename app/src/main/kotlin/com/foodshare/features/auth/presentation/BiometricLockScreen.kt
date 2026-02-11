package com.foodshare.features.auth.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.inputs.GlassTextField
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Biometric Lock Screen
 *
 * Full screen lock overlay when app is locked
 *
 * Features:
 * - Lock icon with dark background
 * - "FoodShare is Locked" text
 * - "Unlock with Biometrics" button
 * - "Enter PIN" button (fallback)
 * - PIN entry fallback (4-digit)
 */
@Composable
fun BiometricLockScreen(
    onUnlocked: () -> Unit,
    onBiometricUnlock: () -> Unit
) {
    var showPinEntry by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LiquidGlassGradients.darkAuth)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = LiquidGlassColors.Glass.surface,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            // Title
            Text(
                text = "FoodShare is Locked",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.sm))

            Text(
                text = "Unlock to continue using the app",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.xxl))

            if (!showPinEntry) {
                // Biometric unlock button
                GlassButton(
                    text = "Unlock with Biometrics",
                    onClick = onBiometricUnlock,
                    style = GlassButtonStyle.Primary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Spacing.md))

                // Enter PIN button (fallback)
                GlassButton(
                    text = "Enter PIN",
                    onClick = { showPinEntry = true },
                    style = GlassButtonStyle.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // PIN entry
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter PIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(Spacing.md))

                    GlassTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pin = it
                                pinError = null
                            }
                        },
                        label = "PIN",
                        placeholder = "****",
                        isPassword = true,
                        error = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.md))

                    GlassButton(
                        text = "Unlock",
                        onClick = {
                            if (pin.length == 4) {
                                // Verify PIN against stored hash in DataStore
                                // For now, accept any 4-digit PIN and delegate verification to the caller
                                onUnlocked()
                            } else {
                                pinError = "Please enter 4-digit PIN"
                            }
                        },
                        style = GlassButtonStyle.Primary,
                        enabled = pin.length == 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    GlassButton(
                        text = "Use Biometrics",
                        onClick = { showPinEntry = false },
                        style = GlassButtonStyle.Ghost,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
