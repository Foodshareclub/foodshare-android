package com.foodshare.core.deeplink

import androidx.navigation.NavController
import com.foodshare.ui.navigation.NavRoutes

/**
 * Maps resolved deep link routes to NavController navigation actions.
 *
 * Uses DeepLinkBridge for parsing, then translates ResolvedRoute
 * to actual Compose Navigation destinations.
 */
object DeepLinkNavigator {

    /**
     * Navigate to a deep link URL.
     *
     * @param navController The NavController to navigate with
     * @param url The deep link URL (foodshare:// or https://foodshare.club/...)
     * @return true if navigation was handled, false otherwise
     */
    fun navigate(navController: NavController, url: String): Boolean {
        val resolved = DeepLinkBridge.parseAndResolve(url) ?: return false
        return navigateToRoute(navController, resolved)
    }

    /**
     * Navigate using a resolved route.
     */
    fun navigateToRoute(navController: NavController, route: ResolvedRoute): Boolean {
        val id = route.getParam("id")

        return when (route.screen) {
            "listing_detail" -> {
                id?.toIntOrNull()?.let {
                    navController.navigate(NavRoutes.listingDetail(it))
                    true
                } ?: false
            }

            "profile" -> {
                navController.navigate(NavRoutes.PROFILE)
                true
            }

            "conversation" -> {
                id?.let {
                    navController.navigate(NavRoutes.conversation(it))
                    true
                } ?: false
            }

            "arrangement_detail" -> {
                id?.let {
                    navController.navigate(NavRoutes.arrangement(it))
                    true
                } ?: false
            }

            "reviews" -> {
                id?.let {
                    navController.navigate(NavRoutes.userReviews(it))
                    true
                } ?: false
            }

            "submit_review" -> {
                id?.let { revieweeId ->
                    val postId = route.getParam("postId")
                    val transactionType = route.getParam("transactionType") ?: "shared"
                    navController.navigate(NavRoutes.submitReview(revieweeId, postId, transactionType))
                    true
                } ?: false
            }

            "messages" -> {
                navController.navigate(NavRoutes.MESSAGES)
                true
            }

            "search" -> {
                navController.navigate(NavRoutes.SEARCH)
                true
            }

            "forum" -> {
                navController.navigate(NavRoutes.FORUM)
                true
            }

            "forum_post_detail" -> {
                id?.toIntOrNull()?.let {
                    navController.navigate(NavRoutes.forumPost(it))
                    true
                } ?: false
            }

            "settings" -> {
                navController.navigate(NavRoutes.SETTINGS)
                true
            }

            "notifications" -> {
                navController.navigate(NavRoutes.NOTIFICATIONS)
                true
            }

            "favorites" -> {
                // Navigate to profile (saved items section)
                navController.navigate(NavRoutes.PROFILE)
                true
            }

            "my_listings" -> {
                navController.navigate(NavRoutes.MY_LISTINGS)
                true
            }

            // Tab switching
            "feed", "explore" -> {
                navController.navigate(NavRoutes.MAIN)
                true
            }

            "challenges" -> {
                navController.navigate(NavRoutes.CHALLENGES)
                true
            }

            else -> false
        }
    }
}
