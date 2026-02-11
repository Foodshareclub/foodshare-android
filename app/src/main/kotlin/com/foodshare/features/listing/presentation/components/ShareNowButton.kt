package com.foodshare.features.listing.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.foodshare.core.share.ShareService
import com.foodshare.ui.design.components.buttons.GlassButtonSmall
import com.foodshare.ui.design.components.buttons.GlassButtonStyle

@Composable
fun ShareNowButton(
    title: String,
    description: String,
    listingId: Int,
    shareService: ShareService,
    modifier: Modifier = Modifier
) {
    GlassButtonSmall(
        text = "Share",
        onClick = { shareService.shareListing(title, description, listingId) },
        icon = Icons.Default.Share,
        style = GlassButtonStyle.Secondary,
        modifier = modifier
    )
}
