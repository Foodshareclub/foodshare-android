package com.foodshare.features.profile.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodshare.core.invitation.SentInvite
import com.foodshare.core.validation.ValidationBridge
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.components.inputs.GlassTextArea
import com.foodshare.ui.design.components.inputs.GlassTextField
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

/**
 * Invite screen for sharing referral links and sending direct invitations.
 *
 * Features:
 * - Referral link display with copy-to-clipboard button
 * - Share via native Android share sheet
 * - Email input for direct invitations with validation
 * - Optional personal message field
 * - History of previously sent invitations with status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InviteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Invite Friends",
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
                .padding(horizontal = Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Referral Link Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        text = "Your Referral Link",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(Spacing.xxs))

                    Text(
                        text = "Share this link with friends to invite them to FoodShare.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    // Referral link with copy button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidGlassColors.Glass.micro)
                            .clickable {
                                copyToClipboard(context, uiState.referralLink)
                            }
                            .padding(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.referralLink,
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlassColors.brandTeal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(Spacing.xxs))

                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy link",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    // Share button
                    GlassButton(
                        text = "Share via...",
                        onClick = {
                            shareReferralLink(context, uiState.shareText)
                        },
                        icon = Icons.Default.Share,
                        style = GlassButtonStyle.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // Direct Invitation Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        text = "Send Direct Invitation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(Spacing.xxs))

                    Text(
                        text = "Send a personalized invitation directly to someone's email.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    // Email input
                    GlassTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = "Email Address",
                        placeholder = "friend@example.com",
                        error = uiState.emailError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.xs))

                    // Personal message
                    GlassTextArea(
                        value = uiState.message,
                        onValueChange = { viewModel.updateMessage(it) },
                        label = "Personal Message (optional)",
                        placeholder = "Hey! Join me on FoodShare...",
                        error = uiState.messageError,
                        helperText = "${uiState.message.length}/${ValidationBridge.MAX_INVITATION_MESSAGE_LENGTH}",
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = LiquidGlassColors.success,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(Spacing.xxs))

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

                    // Send button
                    GlassButton(
                        text = if (uiState.isSending) "Sending..." else "Send Invitation",
                        onClick = { viewModel.sendInvitation() },
                        icon = Icons.AutoMirrored.Filled.Send,
                        style = GlassButtonStyle.PinkTeal,
                        isLoading = uiState.isSending,
                        enabled = uiState.canSend,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // Invitation History Section
            if (uiState.invitesSent.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "Sent Invitations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(Spacing.xs))

                        uiState.invitesSent.forEach { invite ->
                            InviteHistoryRow(invite = invite)
                            Spacer(Modifier.height(Spacing.xxs))
                        }
                    }
                }
            } else if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = LiquidGlassColors.brandTeal,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

/**
 * Row displaying a single sent invitation with its status.
 */
@Composable
private fun InviteHistoryRow(
    invite: SentInvite,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LiquidGlassColors.Glass.micro)
            .padding(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Email icon
        Icon(
            imageVector = Icons.Default.Mail,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(Spacing.xxs))

        // Email and date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = invite.email,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = ValidationBridge.formatDateShort(invite.sentAt),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Spacer(Modifier.width(Spacing.xxs))

        // Status indicator
        InviteStatusIndicator(status = invite.status)
    }
}

/**
 * Status indicator for a sent invitation.
 */
@Composable
private fun InviteStatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status.lowercase()) {
        "accepted" -> LiquidGlassColors.success
        "expired" -> LiquidGlassColors.accentGray
        else -> LiquidGlassColors.warning
    }

    val statusIcon = when (status.lowercase()) {
        "accepted" -> Icons.Default.CheckCircle
        "expired" -> Icons.Default.Timer
        else -> Icons.Default.HourglassEmpty
    }

    val statusLabel = when (status.lowercase()) {
        "accepted" -> "Accepted"
        "expired" -> "Expired"
        else -> "Sent"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = Spacing.xxs, vertical = Spacing.xxxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxxs)
    ) {
        Icon(
            imageVector = statusIcon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(12.dp)
        )

        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
    }
}

/**
 * Copy text to the system clipboard and show a Toast confirmation.
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("FoodShare Referral Link", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
}

/**
 * Open the native Android share sheet with the given share text.
 */
private fun shareReferralLink(context: Context, shareText: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Join FoodShare!")
    }
    val chooser = Intent.createChooser(intent, "Share via")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
