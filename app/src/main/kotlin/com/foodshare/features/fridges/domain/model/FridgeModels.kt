package com.foodshare.features.fridges.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityFridge(
    val id: Int,
    val name: String,
    val description: String? = null,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val status: FridgeStatus = FridgeStatus.ACTIVE,
    @SerialName("stock_level") val stockLevel: StockLevel = StockLevel.UNKNOWN,
    @SerialName("last_stocked_at") val lastStockedAt: String? = null,
    @SerialName("last_checked_at") val lastCheckedAt: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("operating_hours") val operatingHours: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val distance: Double? = null
)

@Serializable
enum class FridgeStatus {
    @SerialName("active") ACTIVE,
    @SerialName("maintenance") MAINTENANCE,
    @SerialName("inactive") INACTIVE
}

@Serializable
enum class StockLevel {
    @SerialName("full") FULL,
    @SerialName("half") HALF,
    @SerialName("low") LOW,
    @SerialName("empty") EMPTY,
    @SerialName("unknown") UNKNOWN
}

@Serializable
data class FridgeReport(
    val id: Int? = null,
    @SerialName("fridge_id") val fridgeId: Int,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("stock_level") val stockLevel: StockLevel,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
