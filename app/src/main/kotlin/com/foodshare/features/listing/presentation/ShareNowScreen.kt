package com.foodshare.features.listing.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.components.inputs.GlassTextArea
import com.foodshare.ui.design.tokens.CornerRadius
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing
import kotlinx.coroutines.launch

// ============================================================================
// Data Models
// ============================================================================

/**
 * Share platform target.
 */
enum class SharePlatform(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    COPY_LINK(
        displayName = "Copy Link",
        icon = Icons.Default.ContentCopy,
        color = Color(0xFF95A5A6) // Gray
    ),
    SMS(
        displayName = "SMS",
        icon = Icons.Default.Sms,
        color = Color(0xFF2ECC71) // Green
    ),
    EMAIL(
        displayName = "Email",
        icon = Icons.Default.Email,
        color = Color(0xFF3498DB) // Blue
    ),
    WHATSAPP(
        displayName = "WhatsApp",
        icon = Icons.Default.Sms, // Placeholder - WhatsApp icon not in Material
        color = Color(0xFF25D366) // WhatsApp green
    ),
    MORE(
        displayName = "More...",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF9B59B6) // Purple
    )
}

/**
 * The type of content being shared.
 */
enum class ShareContentType {
    LISTING,
    FORUM_POST,
    CHALLENGE,
    PROFILE,
    APP
}

/**
 * Data about the content being shared.
 */
data class ShareContent(
    val type: ShareContentType = ShareContentType.APP,
    val id: String? = null,
    val title: String = "FoodShare",
    val description: String = "Join FoodShare and help reduce food waste!",
    val deepLink: String = "https://foodshare.club"
)

// ============================================================================
// ShareNowScreen
// ============================================================================

/**
 * Quick share screen for sharing FoodShare content to various platforms.
 *
 * Features:
 * - Text input for custom share message
 * - Platform selection (Copy Link, SMS, Email, WhatsApp, More...)
 * - Preview of what will be shared
 * - Share button
 * - Deep link generation
 *
 * SYNC: Mirrors Swift ShareNowView
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareNowScreen(
    shareContent: ShareContent = ShareContent(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var customMessage by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf<SharePlatform?>(null) }

    // Build the share text
    val shareText = buildShareText(shareContent, customMessage)
    val shareUrl = shareContent.deepLink

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Share",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier.background(brush = LiquidGlassGradients.darkAuth)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            // Content preview card
            SharePreviewCard(shareContent = shareContent)

            // Custom message input
            ShareMessageInput(
                message = customMessage,
                onMessageChange = { customMessage = it }
            )

            // Platform selection
            Text(
                text = "Share via",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(top = Spacing.sm)
            )

            PlatformSelectionRow(
                selectedPlatform = selectedPlatform,
                onPlatformSelected = { platform ->
                    selectedPlatform = platform
                }
            )

            // Share preview
            AnimatedVisibility(
                visible = selectedPlatform != null || customMessage.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ShareTextPreview(shareText = shareText, shareUrl = shareUrl)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Share button
            GlassButton(
                text = when (selectedPlatform) {
                    SharePlatform.COPY_LINK -> "Copy Link"
                    SharePlatform.SMS -> "Open SMS"
                    SharePlatform.EMAIL -> "Open Email"
                    SharePlatform.WHATSAPP -> "Open WhatsApp"
                    SharePlatform.MORE -> "Share..."
                    null -> "Share"
                },
                onClick = {
                    val platform = selectedPlatform ?: SharePlatform.MORE
                    executeShare(
                        context = context,
                        platform = platform,
                        shareText = shareText,
                        shareUrl = shareUrl,
                        title = shareContent.title,
                        onCopied = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Link copied to clipboard!")
                            }
                        }
                    )
                },
                icon = Icons.AutoMirrored.Filled.Send,
                style = GlassButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

// ============================================================================
// Content Preview Card
// ============================================================================

@Composable
private fun SharePreviewCard(
    shareContent: ShareContent,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.large
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            // Content type badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(CornerRadius.small))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    LiquidGlassColors.brandPink.copy(alpha = 0.8f),
                                    LiquidGlassColors.brandTeal.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xxxs)
                ) {
                    Text(
                        text = when (shareContent.type) {
                            ShareContentType.LISTING -> "Listing"
                            ShareContentType.FORUM_POST -> "Forum Post"
                            ShareContentType.CHALLENGE -> "Challenge"
                            ShareContentType.PROFILE -> "Profile"
                            ShareContentType.APP -> "FoodShare"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Title
            Text(
                text = shareContent.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.xxxs))

            // Description
            Text(
                text = shareContent.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Deep link preview
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(CornerRadius.small))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxxs)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = LiquidGlassColors.brandTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xxxs))
                Text(
                    text = shareContent.deepLink,
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlassColors.brandTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================================
// Message Input
// ============================================================================

@Composable
private fun ShareMessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassTextArea(
        value = message,
        onValueChange = onMessageChange,
        modifier = modifier.fillMaxWidth(),
        label = "Add a message (optional)",
        placeholder = "Say something about what you're sharing...",
        minLines = 2,
        maxLines = 4
    )
}

// ============================================================================
// Platform Selection
// ============================================================================

@Composable
private fun PlatformSelectionRow(
    selectedPlatform: SharePlatform?,
    onPlatformSelected: (SharePlatform) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(horizontal = Spacing.xxxs)
    ) {
        items(SharePlatform.entries) { platform ->
            PlatformButton(
                platform = platform,
                isSelected = selectedPlatform == platform,
                onClick = { onPlatformSelected(platform) }
            )
        }
    }
}

@Composable
private fun PlatformButton(
    platform: SharePlatform,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    color = if (isSelected)
                        platform.color.copy(alpha = 0.3f)
                    else
                        Color.White.copy(alpha = 0.08f)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected)
                        platform.color
                    else
                        Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = platform.icon,
                contentDescription = platform.displayName,
                tint = if (isSelected) platform.color else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xxxs))

        Text(
            text = platform.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected)
                platform.color
            else
                Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ============================================================================
// Share Text Preview
// ============================================================================

@Composable
private fun ShareTextPreview(
    shareText: String,
    shareUrl: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = shareText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(Spacing.xxxs))

            Text(
                text = shareUrl,
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlassColors.brandTeal
            )
        }
    }
}

// ============================================================================
// Utilities
// ============================================================================

/**
 * Build the full share text from content and optional custom message.
 */
private fun buildShareText(content: ShareContent, customMessage: String): String {
    return buildString {
        if (customMessage.isNotBlank()) {
            appendLine(customMessage.trim())
            appendLine()
        }

        when (content.type) {
            ShareContentType.LISTING -> {
                appendLine("Check out \"${content.title}\" on FoodShare!")
                if (content.description.isNotBlank()) {
                    appendLine(content.description.take(100))
                }
            }
            ShareContentType.FORUM_POST -> {
                appendLine("Check out this discussion on FoodShare: \"${content.title}\"")
            }
            ShareContentType.CHALLENGE -> {
                appendLine("Join this FoodShare challenge: \"${content.title}\"")
            }
            ShareContentType.PROFILE -> {
                appendLine("Check out ${content.title}'s profile on FoodShare!")
            }
            ShareContentType.APP -> {
                appendLine("Join FoodShare and help reduce food waste!")
                if (content.description.isNotBlank()) {
                    appendLine(content.description)
                }
            }
        }
    }.trim()
}

/**
 * Execute the share action for the selected platform.
 */
private fun executeShare(
    context: Context,
    platform: SharePlatform,
    shareText: String,
    shareUrl: String,
    title: String,
    onCopied: () -> Unit
) {
    val fullText = "$shareText\n\n$shareUrl"

    when (platform) {
        SharePlatform.COPY_LINK -> {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("FoodShare Link", shareUrl)
            clipboard.setPrimaryClip(clip)
            onCopied()
        }

        SharePlatform.SMS -> {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", fullText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(smsIntent)
            } catch (_: Exception) {
                // Fall back to generic share
                openGenericShare(context, fullText, title)
            }
        }

        SharePlatform.EMAIL -> {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_SUBJECT, "FoodShare: $title")
                putExtra(Intent.EXTRA_TEXT, fullText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(emailIntent)
            } catch (_: Exception) {
                openGenericShare(context, fullText, title)
            }
        }

        SharePlatform.WHATSAPP -> {
            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, fullText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(whatsappIntent)
            } catch (_: Exception) {
                // WhatsApp not installed, fall back
                openGenericShare(context, fullText, title)
            }
        }

        SharePlatform.MORE -> {
            openGenericShare(context, fullText, title)
        }
    }
}

/**
 * Open the system share sheet.
 */
private fun openGenericShare(context: Context, text: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "FoodShare: $title")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooser = Intent.createChooser(intent, "Share via").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
