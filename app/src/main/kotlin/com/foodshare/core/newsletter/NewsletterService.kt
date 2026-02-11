package com.foodshare.core.newsletter

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing newsletter subscriptions via Supabase.
 *
 * Provides subscribe, unsubscribe, and preference management for
 * the FoodShare newsletter. Subscriptions are stored in the
 * `newsletter_subscriptions` table.
 *
 * SYNC: Mirrors Swift NewsletterService
 */
@Singleton
class NewsletterService @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    companion object {
        private const val TAG = "NewsletterService"
        private const val TABLE_NAME = "newsletter_subscriptions"
    }

    // ========================================================================
    // Subscription Management
    // ========================================================================

    /**
     * Subscribe the current user to the newsletter.
     *
     * @param email The email address to subscribe (defaults to auth email)
     * @param preferences Initial newsletter preferences
     * @return Result indicating success or failure
     */
    suspend fun subscribe(
        email: String? = null,
        preferences: NewsletterPreferences = NewsletterPreferences()
    ): Result<NewsletterSubscription> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")
            val subscriberEmail = email
                ?: supabaseClient.auth.currentUserOrNull()?.email
                ?: throw IllegalStateException("No email available")

            val subscription = NewSubscriptionRequest(
                userId = userId,
                email = subscriberEmail,
                isSubscribed = true,
                weeklyDigest = preferences.weeklyDigest,
                newListingsNearby = preferences.newListingsNearby,
                communityUpdates = preferences.communityUpdates,
                challengeReminders = preferences.challengeReminders,
                tips = preferences.tips
            )

            supabaseClient.from(TABLE_NAME)
                .upsert(subscription) {
                    onConflict = "user_id"
                }

            Log.d(TAG, "Successfully subscribed $subscriberEmail to newsletter")

            NewsletterSubscription(
                userId = userId,
                email = subscriberEmail,
                isSubscribed = true,
                preferences = preferences
            )
        }
    }

    /**
     * Unsubscribe the current user from the newsletter.
     *
     * This sets `is_subscribed` to false but preserves the record and
     * preferences so the user can easily re-subscribe later.
     *
     * @return Result indicating success or failure
     */
    suspend fun unsubscribe(): Result<Unit> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            supabaseClient.from(TABLE_NAME)
                .update(mapOf("is_subscribed" to false)) {
                    filter { eq("user_id", userId) }
                }

            Log.d(TAG, "Successfully unsubscribed user from newsletter")
        }
    }

    /**
     * Get the current user's newsletter subscription status and preferences.
     *
     * @return The subscription if it exists, null otherwise
     */
    suspend fun getSubscription(): Result<NewsletterSubscription?> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            supabaseClient.from(TABLE_NAME)
                .select {
                    filter { eq("user_id", userId) }
                    limit(1)
                }
                .decodeSingleOrNull<NewsletterSubscriptionResponse>()
                ?.toSubscription()
        }
    }

    /**
     * Update newsletter preferences for the current user.
     *
     * @param preferences The updated preferences
     * @return Result indicating success or failure
     */
    suspend fun updatePreferences(preferences: NewsletterPreferences): Result<Unit> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            supabaseClient.from(TABLE_NAME)
                .update(
                    mapOf(
                        "weekly_digest" to preferences.weeklyDigest,
                        "new_listings_nearby" to preferences.newListingsNearby,
                        "community_updates" to preferences.communityUpdates,
                        "challenge_reminders" to preferences.challengeReminders,
                        "tips" to preferences.tips
                    )
                ) {
                    filter { eq("user_id", userId) }
                }

            Log.d(TAG, "Successfully updated newsletter preferences")
        }
    }

    /**
     * Check if the current user is subscribed to the newsletter.
     *
     * @return true if subscribed, false otherwise
     */
    suspend fun isSubscribed(): Boolean {
        return try {
            val subscription = getSubscription().getOrNull()
            subscription?.isSubscribed == true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check subscription status: ${e.message}")
            false
        }
    }
}

// ============================================================================
// Data Models
// ============================================================================

/**
 * Newsletter subscription preferences.
 */
data class NewsletterPreferences(
    val weeklyDigest: Boolean = true,
    val newListingsNearby: Boolean = true,
    val communityUpdates: Boolean = true,
    val challengeReminders: Boolean = true,
    val tips: Boolean = false
)

/**
 * Newsletter subscription status and preferences.
 */
data class NewsletterSubscription(
    val userId: String,
    val email: String,
    val isSubscribed: Boolean,
    val preferences: NewsletterPreferences
)

/**
 * Request body for creating/updating a newsletter subscription.
 */
@Serializable
internal data class NewSubscriptionRequest(
    @SerialName("user_id") val userId: String,
    val email: String,
    @SerialName("is_subscribed") val isSubscribed: Boolean,
    @SerialName("weekly_digest") val weeklyDigest: Boolean,
    @SerialName("new_listings_nearby") val newListingsNearby: Boolean,
    @SerialName("community_updates") val communityUpdates: Boolean,
    @SerialName("challenge_reminders") val challengeReminders: Boolean,
    val tips: Boolean
)

/**
 * Response model from the newsletter_subscriptions table.
 */
@Serializable
internal data class NewsletterSubscriptionResponse(
    @SerialName("user_id") val userId: String,
    val email: String,
    @SerialName("is_subscribed") val isSubscribed: Boolean,
    @SerialName("weekly_digest") val weeklyDigest: Boolean = true,
    @SerialName("new_listings_nearby") val newListingsNearby: Boolean = true,
    @SerialName("community_updates") val communityUpdates: Boolean = true,
    @SerialName("challenge_reminders") val challengeReminders: Boolean = true,
    val tips: Boolean = false
) {
    fun toSubscription(): NewsletterSubscription = NewsletterSubscription(
        userId = userId,
        email = email,
        isSubscribed = isSubscribed,
        preferences = NewsletterPreferences(
            weeklyDigest = weeklyDigest,
            newListingsNearby = newListingsNearby,
            communityUpdates = communityUpdates,
            challengeReminders = challengeReminders,
            tips = tips
        )
    )
}
