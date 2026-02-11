package com.foodshare.features.insights.domain.repository

import com.foodshare.features.insights.domain.model.UserInsights

interface InsightsRepository {
    suspend fun getUserInsights(userId: String): Result<UserInsights>
}
