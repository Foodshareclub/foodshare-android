package com.foodshare.ui.design.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.foodshare.ui.design.tokens.LiquidGlassColors
import com.foodshare.ui.design.tokens.Spacing

/**
 * Glass carousel with horizontal pager and glass dot indicators
 *
 * Features:
 * - HorizontalPager for swipeable content
 * - Glass dot indicators at bottom
 * - Active indicator: brandPink
 * - Inactive indicator: Glass.surface
 * - Page-based navigation with smooth animations
 *
 * Note: Uses Compose Foundation's HorizontalPager (built-in)
 * No external accompanist dependency required
 */
@Composable
fun GlassCarousel(
    pageCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable (pageIndex: Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal pager for swipeable content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            content(page)
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Glass dot indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isActive = pagerState.currentPage == index

                Box(
                    modifier = Modifier
                        .size(if (isActive) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isActive) {
                                LiquidGlassColors.brandPink
                            } else {
                                LiquidGlassColors.Glass.surface
                            }
                        )
                )

                // Spacing between dots (except after last dot)
                if (index < pageCount - 1) {
                    Spacer(modifier = Modifier.size(Spacing.xxs))
                }
            }
        }
    }
}
