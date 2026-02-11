package com.foodshare.core.engagement

/**
 * Bridge for engagement scoring logic using Swift implementation.
 *
 * Architecture (Frameo pattern - swift-java):
 * - Uses Swift EngagementEngine via swift-java generated classes
 * - Ensures identical engagement scoring across iOS and Android
 *
 * Swift implementation:
 * - foodshare-core/Sources/FoodshareCore/Engagement/EngagementEngine.swift
 *
 * Formula: posts*10 + comments*5 + likesGiven*1 + likesReceived*2 + reactions*2 + helpful*15
 */
object EngagementBridge {

    // ========================================================================
    // Engagement Score (matching Swift EngagementEngine)
    // ========================================================================

    /**
     * Calculate total engagement score from activity metrics.
     *
     * Formula: posts*10 + comments*5 + likesGiven*1 + likesReceived*2 + reactions*2 + helpful*15
     */
    fun calculateEngagementScore(
        posts: Int,
        comments: Int,
        likesGiven: Int,
        likesReceived: Int,
        reactions: Int,
        helpful: Int
    ): Int {
        return posts * 10 +
            comments * 5 +
            likesGiven * 1 +
            likesReceived * 2 +
            reactions * 2 +
            helpful * 15
    }

    // ========================================================================
    // Activity Level (matching Swift EngagementEngine)
    // ========================================================================

    /**
     * Calculate user activity level based on recency of engagement.
     *
     * @param daysSinceLastPost Days since last post (null if never posted)
     * @param daysSinceLastComment Days since last comment (null if never commented)
     * @return Activity level: "Very Active", "Active", "Moderate", or "Inactive"
     */
    fun calculateActivityLevel(
        daysSinceLastPost: Int?,
        daysSinceLastComment: Int?
    ): String {
        val daysSinceActivity = when {
            daysSinceLastPost != null && daysSinceLastComment != null ->
                minOf(daysSinceLastPost, daysSinceLastComment)
            daysSinceLastPost != null -> daysSinceLastPost
            daysSinceLastComment != null -> daysSinceLastComment
            else -> return "Inactive"
        }

        return when (daysSinceActivity) {
            in 0..1 -> "Very Active"
            in 2..7 -> "Active"
            in 8..30 -> "Moderate"
            else -> "Inactive"
        }
    }

    // ========================================================================
    // Trust Level Progress (matching Swift EngagementEngine)
    // ========================================================================

    /**
     * Calculate progress towards the next trust level (0.0 to 1.0).
     */
    fun calculateTrustLevelProgress(
        currentStats: UserStats,
        requirements: TrustLevelRequirements
    ): Double {
        val progressValues = listOf(
            progress(currentStats.daysSinceJoin, requirements.minDaysSinceJoin),
            progress(currentStats.postsRead, requirements.minPostsRead),
            progress(currentStats.topicsRead, requirements.minTopicsRead),
            progress(currentStats.postsCreated, requirements.minPostsCreated),
            progress(currentStats.likesGiven, requirements.minLikesGiven),
            progress(currentStats.likesReceived, requirements.minLikesReceived),
            progress(currentStats.timeSpentMinutes, requirements.minTimeSpentMinutes)
        )

        return if (progressValues.isEmpty()) 1.0
        else progressValues.sum() / progressValues.size
    }

    private fun progress(current: Int, required: Int): Double {
        if (required <= 0) return 1.0
        return minOf(1.0, current.toDouble() / required.toDouble())
    }
}

/**
 * User stats for trust level calculations.
 */
data class UserStats(
    val daysSinceJoin: Int,
    val postsRead: Int,
    val topicsRead: Int,
    val postsCreated: Int,
    val likesGiven: Int,
    val likesReceived: Int,
    val timeSpentMinutes: Int
)

/**
 * Trust level requirement thresholds.
 */
data class TrustLevelRequirements(
    val minDaysSinceJoin: Int,
    val minPostsRead: Int,
    val minTopicsRead: Int,
    val minPostsCreated: Int,
    val minLikesGiven: Int,
    val minLikesReceived: Int,
    val minTimeSpentMinutes: Int
)
