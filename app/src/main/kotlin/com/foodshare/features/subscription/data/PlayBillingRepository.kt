package com.foodshare.features.subscription.data

import android.app.Activity
import com.foodshare.features.subscription.domain.model.PurchaseResult
import com.foodshare.features.subscription.domain.model.SubscriptionPlan
import com.foodshare.features.subscription.domain.model.SubscriptionStatus
import com.foodshare.features.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingRepository @Inject constructor(
    private val billingService: BillingService
) : SubscriptionRepository {

    override suspend fun getProducts(): Result<List<SubscriptionPlan>> = runCatching {
        billingService.queryProducts()
    }

    override suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult {
        return billingService.launchPurchaseFlow(activity, plan)
    }

    override suspend fun restorePurchases(): SubscriptionStatus {
        return billingService.querySubscriptionStatus()
    }

    override suspend fun getSubscriptionStatus(): SubscriptionStatus {
        return billingService.querySubscriptionStatus()
    }
}
