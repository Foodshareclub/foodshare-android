package com.foodshare.features.support.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodshare.ui.design.components.buttons.GlassButton
import com.foodshare.ui.design.components.buttons.GlassButtonStyle
import com.foodshare.ui.design.components.cards.GlassCard
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.LiquidGlassGradients
import com.foodshare.ui.design.tokens.Spacing

private data class FAQSection(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val items: List<FAQItem>
)

private data class FAQItem(
    val question: String,
    val answer: String
)

private val faqSections = listOf(
    FAQSection(
        title = "Getting Started",
        icon = Icons.Default.PlayCircle,
        color = LiquidGlassColors.success,
        items = listOf(
            FAQItem(
                "How do I create an account?",
                "Tap 'Sign Up' on the welcome screen. You can register with your email address or sign in with Google. Verification is quick and easy."
            ),
            FAQItem(
                "How do I share food?",
                "Tap the '+' button at the bottom of the screen, fill in the details about your food item, add photos, and publish. Nearby users will be notified."
            ),
            FAQItem(
                "How do I find food near me?",
                "Use the Feed tab to browse listings, or switch to the Map view to see food available near your location. You can filter by category and distance."
            ),
            FAQItem(
                "Is Foodshare free to use?",
                "Yes! Foodshare is completely free for all users. We believe everyone should have access to food sharing in their community."
            )
        )
    ),
    FAQSection(
        title = "Food Safety",
        icon = Icons.Default.Shield,
        color = Color(0xFFFF9800),
        items = listOf(
            FAQItem(
                "Is the food safe to eat?",
                "We encourage all sharers to provide accurate descriptions and expiry information. Always check the condition of food before consuming. Use your best judgment."
            ),
            FAQItem(
                "What types of food can be shared?",
                "Most packaged and prepared foods can be shared. Raw meat, dairy past its use-by date, and opened containers are discouraged for safety reasons."
            ),
            FAQItem(
                "What if I receive spoiled food?",
                "Report the listing using the Report button. Our moderation team reviews all reports and takes action to maintain quality standards."
            )
        )
    ),
    FAQSection(
        title = "Using the App",
        icon = Icons.Default.PhoneAndroid,
        color = LiquidGlassColors.brandTeal,
        items = listOf(
            FAQItem(
                "How do I contact a sharer?",
                "Open a listing and tap 'Contact Sharer' to start a conversation. You can arrange pickup details through our secure messaging system."
            ),
            FAQItem(
                "Can I save listings for later?",
                "Yes! Tap the heart icon on any listing to add it to your favorites. Access saved items from your Profile."
            ),
            FAQItem(
                "How do reviews work?",
                "After completing a transaction, both parties can leave reviews. This helps build trust in the community."
            ),
            FAQItem(
                "How do I edit or delete my listing?",
                "Go to My Listings from your Profile, select the listing, and use the edit or delete options."
            )
        )
    ),
    FAQSection(
        title = "Account & Privacy",
        icon = Icons.Default.Lock,
        color = Color(0xFF9C27B0),
        items = listOf(
            FAQItem(
                "How is my data protected?",
                "We use industry-standard encryption and never share your personal information with third parties. Your exact location is never shown to other users."
            ),
            FAQItem(
                "Can I delete my account?",
                "Yes. Go to Settings > Danger Zone > Delete Account. This permanently removes all your data from our servers."
            ),
            FAQItem(
                "How do I change my password?",
                "Go to Settings > Privacy & Security to update your password and enable two-factor authentication for additional security."
            )
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLegalDocument: (String) -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help Center",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        modifier = Modifier.background(brush = LiquidGlassGradients.darkAuth)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.HelpCenter,
                    contentDescription = null,
                    tint = LiquidGlassColors.brandTeal,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // FAQ sections
            faqSections.forEach { section ->
                FAQSectionCard(section = section)
            }

            // Need more help card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Need More Help?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "If you couldn't find what you're looking for, check our legal documents or contact us directly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    GlassButton(
                        text = "Terms of Service",
                        onClick = { onNavigateToLegalDocument("terms") },
                        icon = Icons.Default.Description,
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassButton(
                        text = "Privacy Policy",
                        onClick = { onNavigateToLegalDocument("privacy") },
                        icon = Icons.Default.Policy,
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassButton(
                        text = "Email Support",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@foodshare.club")
                                putExtra(Intent.EXTRA_SUBJECT, "Foodshare Help Request")
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        },
                        icon = Icons.Default.Email,
                        style = GlassButtonStyle.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun FAQSectionCard(section: FAQSection) {
    var isExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevron"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.animateContentSize(animationSpec = tween(300))
        ) {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(section.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = section.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(Spacing.md))

                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${section.items.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Spacer(Modifier.width(Spacing.xs))

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(chevronRotation)
                )
            }

            // FAQ items
            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(
                        start = Spacing.md,
                        end = Spacing.md,
                        bottom = Spacing.md
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    section.items.forEach { item ->
                        FAQItemCard(item = item, accentColor = section.color)
                    }
                }
            }
        }
    }
}

@Composable
private fun FAQItemCard(item: FAQItem, accentColor: Color) {
    var isExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(200),
        label = "faq_chevron"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LiquidGlassColors.Glass.micro)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(animationSpec = tween(200))
            .padding(Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronRotation)
            )
        }

        if (isExpanded) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = item.answer,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
