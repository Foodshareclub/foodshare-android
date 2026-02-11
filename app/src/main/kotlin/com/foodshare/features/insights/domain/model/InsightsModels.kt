package com.foodshare.features.insights.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInsights(
    @SerialName("items_shared") val itemsShared: Int = 0,
    @SerialName("items_received") val itemsReceived: Int = 0,
    @SerialName("food_saved_kg") val foodSavedKg: Double = 0.0,
    @SerialName("co2_saved_kg") val co2SavedKg: Double = 0.0,
    @SerialName("water_saved_liters") val waterSavedLiters: Double = 0.0,
    @SerialName("money_saved") val moneySaved: Double = 0.0,
    @SerialName("streak_days") val streakDays: Int = 0,
    @SerialName("monthly_stats") val monthlyStats: List<MonthlyStats> = emptyList(),
    @SerialName("category_stats") val categoryStats: List<CategoryStat> = emptyList()
)

@Serializable
data class MonthlyStats(
    val month: String,
    val shared: Int = 0,
    val received: Int = 0
)

@Serializable
data class CategoryStat(
    val category: String,
    val count: Int = 0,
    val percentage: Double = 0.0
)
