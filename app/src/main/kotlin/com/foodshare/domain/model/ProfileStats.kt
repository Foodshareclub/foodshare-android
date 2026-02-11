package com.foodshare.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileStats(
    @SerialName("items_shared") val itemsShared: Int = 0,
    @SerialName("items_received") val itemsReceived: Int = 0,
    @SerialName("rating_average") val ratingAverage: Double? = null,
    @SerialName("rating_count") val ratingCount: Int = 0,
    @SerialName("total_conversations") val totalConversations: Int = 0,
    @SerialName("challenges_completed") val challengesCompleted: Int = 0,
    @SerialName("forum_posts") val forumPosts: Int = 0,
    @SerialName("food_saved_kg") val foodSavedKg: Double = 0.0
) {
    val formattedRating: String
        get() = ratingAverage?.let { String.format("%.1f", it) } ?: "N/A"

    val totalActivity: Int
        get() = itemsShared + itemsReceived
}
