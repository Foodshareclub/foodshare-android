package com.foodshare.features.fridges.domain.repository

import com.foodshare.features.fridges.domain.model.CommunityFridge
import com.foodshare.features.fridges.domain.model.FridgeReport
import com.foodshare.features.fridges.domain.model.StockLevel
import kotlinx.coroutines.flow.Flow

interface FridgeRepository {
    fun getNearbyFridges(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Flow<List<CommunityFridge>>

    suspend fun getFridgeDetail(id: Int): Result<CommunityFridge>

    suspend fun reportStock(
        fridgeId: Int,
        stockLevel: StockLevel,
        notes: String?
    ): Result<Unit>

    suspend fun getFridgeReports(fridgeId: Int): Result<List<FridgeReport>>
}
