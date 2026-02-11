package com.foodshare.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Fetches data from Supabase for all FoodShare widgets.
 *
 * Caches results in SharedPreferences so widgets can display data
 * offline or when the network is unavailable. Each data type has its
 * own cache key and timestamp for independent staleness checks.
 *
 * Data sources:
 * - Nearby listings: `posts` table filtered by active + not arranged
 * - User stats: `user_stats` view or calculated from multiple tables
 * - Active challenge: `challenge_participants` joined with `challenges`
 *
 * SYNC: Mirrors Swift WidgetDataService
 */
object WidgetDataService {

    private const val TAG = "WidgetDataService"
    private const val PREFS_NAME = "foodshare_widget_data"

    // Cache keys
    private const val KEY_NEARBY_LISTINGS = "nearby_listings"
    private const val KEY_USER_STATS = "user_stats"
    private const val KEY_ACTIVE_CHALLENGE = "active_challenge"
    private const val KEY_TIMESTAMP_PREFIX = "timestamp_"

    // Cache TTL
    private const val CACHE_TTL_MS = 30 * 60 * 1000L // 30 minutes

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    // ========================================================================
    // SharedPreferences Access
    // ========================================================================

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun saveToCache(context: Context, key: String, value: String) {
        getPrefs(context).edit()
            .putString(key, value)
            .putLong("${KEY_TIMESTAMP_PREFIX}$key", System.currentTimeMillis())
            .apply()
    }

    private fun loadFromCache(context: Context, key: String): String? {
        return getPrefs(context).getString(key, null)
    }

    private fun isCacheValid(context: Context, key: String): Boolean {
        val timestamp = getPrefs(context).getLong("${KEY_TIMESTAMP_PREFIX}$key", 0)
        return System.currentTimeMillis() - timestamp < CACHE_TTL_MS
    }

    // ========================================================================
    // Nearby Listings
    // ========================================================================

    /**
     * Get nearby food listings for the widget.
     *
     * Returns cached data if available and fresh, otherwise returns
     * an empty list. Use [refreshNearbyListings] to fetch fresh data.
     *
     * @param context Application context
     * @return List of nearby listings for display
     */
    fun getNearbyListings(context: Context): List<WidgetListing> {
        val cached = loadFromCache(context, KEY_NEARBY_LISTINGS) ?: return emptyList()
        return try {
            val responses = json.decodeFromString<List<WidgetListingResponse>>(cached)
            responses.map { it.toWidgetListing() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached listings: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetch fresh nearby listings from Supabase and update the cache.
     *
     * @param context Application context
     * @param supabaseClient The Supabase client
     * @return List of nearby listings
     */
    suspend fun refreshNearbyListings(
        context: Context,
        supabaseClient: SupabaseClient
    ): List<WidgetListing> {
        return try {
            val listings = supabaseClient.from("posts")
                .select {
                    filter {
                        eq("is_active", true)
                        eq("is_arranged", false)
                    }
                    order("id", Order.DESCENDING)
                    limit(5)
                }
                .decodeList<WidgetListingResponse>()

            val serialized = json.encodeToString(listings)
            saveToCache(context, KEY_NEARBY_LISTINGS, serialized)

            Log.d(TAG, "Refreshed ${listings.size} nearby listings")
            listings.map { it.toWidgetListing() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh nearby listings: ${e.message}")
            getNearbyListings(context) // Fall back to cache
        }
    }

    // ========================================================================
    // User Stats
    // ========================================================================

    /**
     * Get user stats for the widget.
     *
     * Returns cached data if available, otherwise returns default stats.
     *
     * @param context Application context
     * @return User stats for display
     */
    fun getUserStats(context: Context): WidgetUserStats {
        val cached = loadFromCache(context, KEY_USER_STATS) ?: return WidgetUserStats()
        return try {
            val response = json.decodeFromString<WidgetStatsResponse>(cached)
            response.toWidgetStats()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached stats: ${e.message}")
            WidgetUserStats()
        }
    }

    /**
     * Fetch fresh user stats from Supabase and update the cache.
     *
     * @param context Application context
     * @param supabaseClient The Supabase client
     * @return User stats
     */
    suspend fun refreshUserStats(
        context: Context,
        supabaseClient: SupabaseClient
    ): WidgetUserStats {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return getUserStats(context)

            val stats = supabaseClient.from("user_stats")
                .select {
                    filter { eq("user_id", userId) }
                    limit(1)
                }
                .decodeSingleOrNull<WidgetStatsResponse>()
                ?: return WidgetUserStats()

            val serialized = json.encodeToString(stats)
            saveToCache(context, KEY_USER_STATS, serialized)

            Log.d(TAG, "Refreshed user stats")
            stats.toWidgetStats()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh user stats: ${e.message}")
            getUserStats(context) // Fall back to cache
        }
    }

    // ========================================================================
    // Active Challenge
    // ========================================================================

    /**
     * Get the active challenge for the widget.
     *
     * Returns cached data if available, otherwise returns null.
     *
     * @param context Application context
     * @return Active challenge or null if none
     */
    fun getActiveChallenge(context: Context): WidgetChallenge? {
        val cached = loadFromCache(context, KEY_ACTIVE_CHALLENGE) ?: return null
        return try {
            val response = json.decodeFromString<WidgetChallengeResponse>(cached)
            response.toWidgetChallenge()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached challenge: ${e.message}")
            null
        }
    }

    /**
     * Fetch the active challenge from Supabase and update the cache.
     *
     * @param context Application context
     * @param supabaseClient The Supabase client
     * @return Active challenge or null
     */
    suspend fun refreshActiveChallenge(
        context: Context,
        supabaseClient: SupabaseClient
    ): WidgetChallenge? {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return getActiveChallenge(context)

            val challenge = supabaseClient.from("challenge_participants")
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        "challenge_id, current_count, challenges(id, title, description, emoji, " +
                            "target_count, end_date, reward_points)"
                    )
                ) {
                    filter {
                        eq("user_id", userId)
                        eq("status", "active")
                    }
                    limit(1)
                }
                .decodeSingleOrNull<WidgetChallengeResponse>()

            if (challenge != null) {
                val serialized = json.encodeToString(challenge)
                saveToCache(context, KEY_ACTIVE_CHALLENGE, serialized)
                Log.d(TAG, "Refreshed active challenge: ${challenge.challenges?.title}")
            } else {
                // Clear cache if no active challenge
                getPrefs(context).edit().remove(KEY_ACTIVE_CHALLENGE).apply()
                Log.d(TAG, "No active challenge found")
            }

            challenge?.toWidgetChallenge()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh active challenge: ${e.message}")
            getActiveChallenge(context) // Fall back to cache
        }
    }

    // ========================================================================
    // Bulk Refresh
    // ========================================================================

    /**
     * Refresh all widget data at once.
     *
     * Called by WidgetRefreshWorker periodically.
     *
     * @param context Application context
     * @param supabaseClient The Supabase client
     */
    suspend fun refreshAll(context: Context, supabaseClient: SupabaseClient) {
        Log.d(TAG, "Starting full widget data refresh")
        val startTime = System.currentTimeMillis()

        refreshNearbyListings(context, supabaseClient)
        refreshUserStats(context, supabaseClient)
        refreshActiveChallenge(context, supabaseClient)

        val elapsed = System.currentTimeMillis() - startTime
        Log.d(TAG, "Widget data refresh completed in ${elapsed}ms")
    }

    /**
     * Clear all cached widget data.
     *
     * @param context Application context
     */
    fun clearCache(context: Context) {
        getPrefs(context).edit().clear().apply()
        Log.d(TAG, "Widget data cache cleared")
    }
}

// ============================================================================
// API Response Models
// ============================================================================

/**
 * Listing response from Supabase for widget display.
 */
@Serializable
internal data class WidgetListingResponse(
    val id: Int,
    @SerialName("post_name") val postName: String,
    @SerialName("post_type") val postType: String? = null,
    @SerialName("post_address") val postAddress: String? = null,
    @SerialName("distance_meters") val distanceMeters: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val images: List<String>? = null
) {
    fun toWidgetListing(): WidgetListing {
        return WidgetListing(
            id = id,
            title = postName,
            postType = postType,
            distance = formatDistance(distanceMeters),
            timeAgo = formatTimeAgo(createdAt),
            imageUrl = images?.firstOrNull()
        )
    }
}

/**
 * Stats response from Supabase.
 */
@Serializable
internal data class WidgetStatsResponse(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("items_shared") val itemsShared: Int = 0,
    @SerialName("items_received") val itemsReceived: Int = 0,
    @SerialName("community_rank") val communityRank: Int = 0,
    @SerialName("co2_saved_kg") val co2SavedKg: Double = 0.0,
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toWidgetStats(): WidgetUserStats {
        return WidgetUserStats(
            itemsShared = itemsShared,
            itemsReceived = itemsReceived,
            communityRank = communityRank,
            co2SavedKg = co2SavedKg,
            currentStreak = currentStreak,
            lastUpdated = formatTimeAgo(updatedAt)
        )
    }
}

/**
 * Challenge participation response from Supabase.
 */
@Serializable
internal data class WidgetChallengeResponse(
    @SerialName("challenge_id") val challengeId: Int? = null,
    @SerialName("current_count") val currentCount: Int = 0,
    val challenges: ChallengeDetails? = null
) {
    fun toWidgetChallenge(): WidgetChallenge? {
        val details = challenges ?: return null
        return WidgetChallenge(
            id = details.id,
            title = details.title,
            description = details.description ?: "",
            emoji = details.emoji ?: "\uD83C\uDFC6",
            currentCount = currentCount,
            targetCount = details.targetCount,
            daysRemaining = calculateDaysRemaining(details.endDate),
            rewardPoints = details.rewardPoints
        )
    }
}

@Serializable
internal data class ChallengeDetails(
    val id: Int,
    val title: String,
    val description: String? = null,
    val emoji: String? = null,
    @SerialName("target_count") val targetCount: Int = 1,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("reward_points") val rewardPoints: Int = 0
)

// ============================================================================
// Formatting Helpers
// ============================================================================

/**
 * Format distance in meters to a human-readable string.
 */
internal fun formatDistance(meters: Double?): String? {
    if (meters == null) return null
    return when {
        meters < 100 -> "< 100m"
        meters < 1000 -> "${meters.toInt()}m"
        meters < 10000 -> String.format("%.1f km", meters / 1000)
        else -> String.format("%.0f km", meters / 1000)
    }
}

/**
 * Format a timestamp into a relative time string (e.g., "2h ago", "3d ago").
 */
internal fun formatTimeAgo(isoTimestamp: String?): String {
    if (isoTimestamp == null) return "recently"

    return try {
        val instant = Instant.parse(isoTimestamp)
        val now = Instant.now()
        val duration = Duration.between(instant, now)

        when {
            duration.toMinutes() < 1 -> "just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            duration.toDays() < 7 -> "${duration.toDays()}d ago"
            duration.toDays() < 30 -> "${duration.toDays() / 7}w ago"
            else -> "${duration.toDays() / 30}mo ago"
        }
    } catch (e: Exception) {
        "recently"
    }
}

/**
 * Calculate days remaining until the given end date.
 */
internal fun calculateDaysRemaining(endDate: String?): Int {
    if (endDate == null) return 0

    return try {
        val end = Instant.parse(endDate)
        val now = Instant.now()
        val duration = Duration.between(now, end)
        maxOf(0, duration.toDays().toInt())
    } catch (e: Exception) {
        try {
            // Try parsing as date-only format
            val localDate = java.time.LocalDate.parse(endDate)
            val today = java.time.LocalDate.now()
            val days = java.time.temporal.ChronoUnit.DAYS.between(today, localDate)
            maxOf(0, days.toInt())
        } catch (e2: Exception) {
            0
        }
    }
}
