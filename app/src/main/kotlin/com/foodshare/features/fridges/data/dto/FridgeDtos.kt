package com.foodshare.features.fridges.data.dto

import com.foodshare.features.fridges.domain.model.CommunityFridge
import com.foodshare.features.fridges.domain.model.FridgeReport
import com.foodshare.features.fridges.domain.model.FridgeStatus
import com.foodshare.features.fridges.domain.model.StockLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityFridgeDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val status: String = "active",
    @SerialName("stock_level") val stockLevel: String = "unknown",
    @SerialName("last_stocked_at") val lastStockedAt: String? = null,
    @SerialName("last_checked_at") val lastCheckedAt: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("operating_hours") val operatingHours: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val distance: Double? = null
)

@Serializable
data class FridgeReportDto(
    val id: Int? = null,
    @SerialName("fridge_id") val fridgeId: Int,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("stock_level") val stockLevel: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ReportStockRequest(
    @SerialName("fridge_id") val fridgeId: Int,
    @SerialName("stock_level") val stockLevel: String,
    val notes: String? = null
)

fun CommunityFridgeDto.toDomain(): CommunityFridge {
    return CommunityFridge(
        id = id,
        name = name,
        description = description,
        address = address,
        latitude = latitude,
        longitude = longitude,
        status = when (status.lowercase()) {
            "active" -> FridgeStatus.ACTIVE
            "maintenance" -> FridgeStatus.MAINTENANCE
            "inactive" -> FridgeStatus.INACTIVE
            else -> FridgeStatus.ACTIVE
        },
        stockLevel = when (stockLevel.lowercase()) {
            "full" -> StockLevel.FULL
            "half" -> StockLevel.HALF
            "low" -> StockLevel.LOW
            "empty" -> StockLevel.EMPTY
            else -> StockLevel.UNKNOWN
        },
        lastStockedAt = lastStockedAt,
        lastCheckedAt = lastCheckedAt,
        photoUrl = photoUrl,
        operatingHours = operatingHours,
        createdAt = createdAt,
        distance = distance
    )
}

fun FridgeReportDto.toDomain(): FridgeReport {
    return FridgeReport(
        id = id,
        fridgeId = fridgeId,
        userId = userId,
        stockLevel = when (stockLevel.lowercase()) {
            "full" -> StockLevel.FULL
            "half" -> StockLevel.HALF
            "low" -> StockLevel.LOW
            "empty" -> StockLevel.EMPTY
            else -> StockLevel.UNKNOWN
        },
        notes = notes,
        createdAt = createdAt
    )
}
