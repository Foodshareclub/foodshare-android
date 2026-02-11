package com.foodshare.features.forum.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Forum badge domain models.
 *
 * SYNC: This mirrors Swift ForumBadge (ForumBadge.swift)
 * Maps to `forum_badges` and `forum_user_badges` tables.
 */
@Serializable
data class ForumBadge(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    @SerialName("icon_name") val iconName: String? = null,
    val color: String? = null,
    @SerialName("badge_type") val badgeType: BadgeType = BadgeType.ACHIEVEMENT,
    val criteria: BadgeCriteria? = null,
    val points: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
) {
    // ========================================================================
    // Computed Properties (matching iOS ForumBadge)
    // ========================================================================

    /**
     * Badge rarity based on points (matching iOS rarity ranges).
     */
    val rarity: BadgeRarity
        get() = when (points) {
            in 0..24 -> BadgeRarity.COMMON
            in 25..99 -> BadgeRarity.UNCOMMON
            in 100..249 -> BadgeRarity.RARE
            in 250..499 -> BadgeRarity.EPIC
            else -> BadgeRarity.LEGENDARY
        }

    /**
     * Whether this badge has specific auto-award criteria.
     */
    val hasAutoCriteria: Boolean
        get() = criteria != null && !criteria.isEmpty

    /**
     * Maps Lucide/Feather icon names (from DB) to Material icon names.
     *
     * Matching the iOS sfSymbolName mapping.
     */
    val materialIconName: String
        get() = mapIconName(iconName)

    /**
     * Hex color string, with fallback to green.
     */
    val resolvedColor: String
        get() = color ?: "#22c55e"

    companion object {
        /**
         * Map Lucide/Feather icon names to Material icon names.
         */
        fun mapIconName(lucideIcon: String?): String = when (lucideIcon) {
            "pencil" -> "edit"
            "edit-3" -> "draw"
            "book-open" -> "menu_book"
            "award" -> "emoji_events"
            "message-circle" -> "chat_bubble"
            "messages-square" -> "forum"
            "users" -> "group"
            "lightbulb" -> "lightbulb"
            "sparkles" -> "auto_awesome"
            "trophy" -> "emoji_events"
            "star" -> "star"
            "medal" -> "military_tech"
            "shield-check" -> "verified_user"
            "crown" -> "emoji_events"
            "heart" -> "favorite"
            "trending-up" -> "trending_up"
            "clock" -> "schedule"
            "badge-check" -> "verified"
            "shield" -> "shield"
            "leaf" -> "eco"
            "chef-hat" -> "restaurant"
            "help-circle" -> "help"
            "map-pin" -> "place"
            "map" -> "map"
            "home" -> "home"
            "gift" -> "card_giftcard"
            "camera" -> "photo_camera"
            "image" -> "image"
            "share" -> "share"
            "bookmark" -> "bookmark"
            "flag" -> "flag"
            "bell" -> "notifications"
            "settings" -> "settings"
            "search" -> "search"
            "check" -> "check"
            "check-circle" -> "check_circle"
            "x" -> "close"
            "x-circle" -> "cancel"
            "info" -> "info"
            "alert-circle" -> "error"
            "alert-triangle" -> "warning"
            "zap" -> "bolt"
            "fire" -> "local_fire_department"
            "coffee" -> "coffee"
            "utensils" -> "restaurant"
            "package" -> "inventory_2"
            "truck" -> "local_shipping"
            "calendar" -> "calendar_today"
            "user" -> "person"
            "user-plus" -> "person_add"
            "user-check" -> "how_to_reg"
            "thumbs-up" -> "thumb_up"
            "thumbs-down" -> "thumb_down"
            "smile" -> "sentiment_satisfied"
            "frown" -> "sentiment_dissatisfied"
            "sun" -> "light_mode"
            "moon" -> "dark_mode"
            else -> "star"
        }
    }
}

// ========================================================================
// Badge Type
// ========================================================================

/**
 * Types of forum badges.
 *
 * SYNC: This mirrors Swift BadgeType
 */
@Serializable
enum class BadgeType {
    @SerialName("milestone") MILESTONE,
    @SerialName("achievement") ACHIEVEMENT,
    @SerialName("special") SPECIAL;

    val displayName: String
        get() = when (this) {
            MILESTONE -> "Milestone"
            ACHIEVEMENT -> "Achievement"
            SPECIAL -> "Special"
        }

    val icon: String
        get() = when (this) {
            MILESTONE -> "flag"
            ACHIEVEMENT -> "emoji_events"
            SPECIAL -> "auto_awesome"
        }

    val sortOrder: Int
        get() = when (this) {
            SPECIAL -> 0
            ACHIEVEMENT -> 1
            MILESTONE -> 2
        }
}

// ========================================================================
// Badge Criteria
// ========================================================================

/**
 * Criteria for automatic badge awarding.
 *
 * SYNC: This mirrors Swift BadgeCriteria
 */
@Serializable
data class BadgeCriteria(
    @SerialName("posts_count") val postsCount: Int? = null,
    @SerialName("comments_count") val commentsCount: Int? = null,
    @SerialName("helpful_count") val helpfulCount: Int? = null,
    @SerialName("followers_count") val followersCount: Int? = null,
    @SerialName("reputation_score") val reputationScore: Int? = null
) {
    /** Whether all criteria fields are null. */
    val isEmpty: Boolean
        get() = postsCount == null &&
            commentsCount == null &&
            helpfulCount == null &&
            followersCount == null &&
            reputationScore == null

    /**
     * Check if user stats meet this criteria.
     */
    fun isMet(stats: ForumUserStats): Boolean {
        if (postsCount != null && stats.postsCount < postsCount) return false
        if (commentsCount != null && stats.commentsCount < commentsCount) return false
        if (helpfulCount != null && stats.helpfulCount < helpfulCount) return false
        if (followersCount != null && stats.followersCount < followersCount) return false
        if (reputationScore != null && stats.reputationScore < reputationScore) return false
        return true
    }

    /**
     * Progress towards meeting this criteria (0.0 to 1.0).
     */
    fun progress(stats: ForumUserStats): Double {
        val progressValues = mutableListOf<Double>()

        postsCount?.let { required ->
            if (required > 0) {
                progressValues.add(minOf(1.0, stats.postsCount.toDouble() / required))
            }
        }
        commentsCount?.let { required ->
            if (required > 0) {
                progressValues.add(minOf(1.0, stats.commentsCount.toDouble() / required))
            }
        }
        helpfulCount?.let { required ->
            if (required > 0) {
                progressValues.add(minOf(1.0, stats.helpfulCount.toDouble() / required))
            }
        }
        followersCount?.let { required ->
            if (required > 0) {
                progressValues.add(minOf(1.0, stats.followersCount.toDouble() / required))
            }
        }
        reputationScore?.let { required ->
            if (required > 0) {
                progressValues.add(minOf(1.0, stats.reputationScore.toDouble() / required))
            }
        }

        return if (progressValues.isEmpty()) 0.0
        else progressValues.sum() / progressValues.size
    }

    /**
     * Description of what's required.
     */
    val requirementDescription: String?
        get() {
            val parts = mutableListOf<String>()
            postsCount?.let { parts.add("$it posts") }
            commentsCount?.let { parts.add("$it comments") }
            helpfulCount?.let { parts.add("$it helpful reactions") }
            followersCount?.let { parts.add("$it followers") }
            reputationScore?.let { parts.add("$it reputation") }
            return if (parts.isEmpty()) null else parts.joinToString(", ")
        }
}

// ========================================================================
// Badge Rarity
// ========================================================================

/**
 * Badge rarity tiers based on point value.
 *
 * SYNC: This mirrors Swift BadgeRarity
 */
enum class BadgeRarity(val displayName: String, val colorHex: Long, val glowIntensity: Double) {
    COMMON("Common", 0xFF9E9E9E, 0.0),
    UNCOMMON("Uncommon", 0xFF4CAF50, 0.1),
    RARE("Rare", 0xFF2196F3, 0.2),
    EPIC("Epic", 0xFF9C27B0, 0.3),
    LEGENDARY("Legendary", 0xFFFF9800, 0.4);
}

// ========================================================================
// User Badge
// ========================================================================

/**
 * Represents a badge awarded to a user.
 *
 * SYNC: This mirrors Swift UserBadge
 * Maps to `forum_user_badges` table.
 */
@Serializable
data class UserBadge(
    val id: String,
    @SerialName("profile_id") val profileId: String,
    @SerialName("badge_id") val badgeId: Int,
    @SerialName("awarded_at") val awardedAt: String? = null,
    @SerialName("awarded_by") val awardedBy: String? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false
)

// ========================================================================
// User Badge with Details
// ========================================================================

/**
 * User badge joined with badge details for display.
 *
 * SYNC: This mirrors Swift UserBadgeWithDetails
 */
data class UserBadgeWithDetails(
    val userBadge: UserBadge,
    val badge: ForumBadge
)

// ========================================================================
// Badge Collection
// ========================================================================

/**
 * A collection of badges grouped by type.
 *
 * SYNC: This mirrors Swift BadgeCollection
 */
data class BadgeCollection(
    val allBadges: List<ForumBadge>,
    val earnedBadges: List<UserBadgeWithDetails>,
    val featuredBadges: List<UserBadgeWithDetails>
) {
    /** Badges grouped by type. */
    val badgesByType: Map<BadgeType, List<ForumBadge>>
        get() = allBadges.groupBy { it.badgeType }

    /** Earned badge IDs for quick lookup. */
    val earnedBadgeIds: Set<Int>
        get() = earnedBadges.map { it.badge.id }.toSet()

    /** Check if user has earned a specific badge. */
    fun hasEarned(badge: ForumBadge): Boolean =
        earnedBadgeIds.contains(badge.id)

    /** Get user badge for a specific badge ID. */
    fun userBadge(badgeId: Int): UserBadgeWithDetails? =
        earnedBadges.firstOrNull { it.badge.id == badgeId }

    /** Total points from earned badges. */
    val totalPoints: Int
        get() = earnedBadges.sumOf { it.badge.points }

    /**
     * Badges user can earn next (not earned, has criteria, sorted by progress desc).
     */
    fun nextBadges(stats: ForumUserStats): List<ForumBadge> =
        allBadges
            .filter { !earnedBadgeIds.contains(it.id) && it.hasAutoCriteria }
            .sortedByDescending { it.criteria?.progress(stats) ?: 0.0 }

    companion object {
        val empty = BadgeCollection(
            allBadges = emptyList(),
            earnedBadges = emptyList(),
            featuredBadges = emptyList()
        )
    }
}
