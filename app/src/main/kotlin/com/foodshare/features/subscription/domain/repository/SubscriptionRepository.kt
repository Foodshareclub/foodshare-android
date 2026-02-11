package com.foodshare.features.subscription.domain.repository

import android.app.Activity
import com.foodshare.features.subscription.domain.model.PurchaseResult
import com.foodshare.features.subscription.domain.model.SubscriptionPlan
import com.foodshare.features.subscription.domain.model.SubscriptionStatus

interface SubscriptionRepository {
    suspend fun getProducts(): Result<List<SubscriptionPlan>>
    suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult
    suspend fun restorePurchases(): SubscriptionStatus
    suspend fun getSubscriptionStatus(): SubscriptionStatus
}
