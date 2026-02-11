package com.foodshare.features.arrangement.domain.repository

import com.foodshare.features.arrangement.domain.model.Arrangement
import com.foodshare.features.arrangement.domain.model.ArrangementStatus
import kotlinx.coroutines.flow.Flow

interface ArrangementRepository {
    fun observeArrangements(userId: String): Flow<List<Arrangement>>
    suspend fun getArrangement(id: String): Result<Arrangement>
    suspend fun createArrangement(
        listingId: Int,
        ownerId: String,
        pickupDate: String?,
        pickupTime: String?,
        pickupLocation: String?,
        notes: String?
    ): Result<Arrangement>
    suspend fun updateStatus(id: String, status: ArrangementStatus): Result<Arrangement>
    suspend fun getArrangementsForListing(listingId: Int): Result<List<Arrangement>>
}
