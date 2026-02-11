package com.foodshare.features.subscription.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.foodshare.features.subscription.domain.model.PurchaseResult
import com.foodshare.features.subscription.domain.model.SubscriptionPeriod
import com.foodshare.features.subscription.domain.model.SubscriptionPlan
import com.foodshare.features.subscription.domain.model.SubscriptionStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingService @Inject constructor(
    private val context: Context
) {
    companion object {
        const val MONTHLY_PRODUCT_ID = "foodshare_premium_monthly"
        const val YEARLY_PRODUCT_ID = "foodshare_premium_yearly"
    }

    private var billingClient: BillingClient? = null
    private var purchaseDeferred: CompletableDeferred<PurchaseResult>? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgePurchaseIfNeeded(purchase)
                        purchaseDeferred?.complete(PurchaseResult.Success)
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        purchaseDeferred?.complete(PurchaseResult.Pending)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseDeferred?.complete(PurchaseResult.Cancelled)
            }
            else -> {
                purchaseDeferred?.complete(
                    PurchaseResult.Error(billingResult.debugMessage ?: "Purchase failed")
                )
            }
        }
    }

    suspend fun ensureConnected(): Boolean {
        val client = billingClient ?: createClient()
        if (client.isReady) return true

        return suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(true)
                    } else {
                        continuation.resume(false)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Will retry on next operation
                }
            })
        }
    }

    private fun createClient(): BillingClient {
        val client = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient = client
        return client
    }

    suspend fun queryProducts(): List<SubscriptionPlan> {
        if (!ensureConnected()) return emptyList()
        val client = billingClient ?: return emptyList()

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MONTHLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(YEARLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result: ProductDetailsResult = client.queryProductDetails(params)

        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return emptyList()
        }

        return result.productDetailsList?.mapNotNull { details ->
            val offerDetails = details.subscriptionOfferDetails?.firstOrNull() ?: return@mapNotNull null
            val pricingPhase = offerDetails.pricingPhases.pricingPhaseList.firstOrNull() ?: return@mapNotNull null

            val period = when (details.productId) {
                MONTHLY_PRODUCT_ID -> SubscriptionPeriod.MONTHLY
                YEARLY_PRODUCT_ID -> SubscriptionPeriod.YEARLY
                else -> return@mapNotNull null
            }

            SubscriptionPlan(
                productId = details.productId,
                period = period,
                price = pricingPhase.priceAmountMicros,
                formattedPrice = pricingPhase.formattedPrice,
                currencyCode = pricingPhase.priceCurrencyCode
            )
        } ?: emptyList()
    }

    suspend fun launchPurchaseFlow(activity: Activity, plan: SubscriptionPlan): PurchaseResult {
        if (!ensureConnected()) return PurchaseResult.Error("Billing service not available")
        val client = billingClient ?: return PurchaseResult.Error("Billing client not initialized")

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(plan.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = client.queryProductDetails(params)
        val productDetails = result.productDetailsList?.firstOrNull()
            ?: return PurchaseResult.Error("Product not found")

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return PurchaseResult.Error("No offer available")

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        purchaseDeferred = CompletableDeferred()
        val billingResult = client.launchBillingFlow(activity, flowParams)

        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchaseDeferred?.await() ?: PurchaseResult.Error("Purchase flow interrupted")
        } else {
            PurchaseResult.Error(billingResult.debugMessage ?: "Failed to launch purchase")
        }
    }

    suspend fun querySubscriptionStatus(): SubscriptionStatus {
        if (!ensureConnected()) return SubscriptionStatus.NONE
        val client = billingClient ?: return SubscriptionStatus.NONE

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return SubscriptionStatus.NONE
        }

        val activePurchase = result.purchasesList.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.products.any { it in listOf(MONTHLY_PRODUCT_ID, YEARLY_PRODUCT_ID) }
        }

        return when {
            activePurchase != null -> SubscriptionStatus.ACTIVE
            else -> SubscriptionStatus.NONE
        }
    }

    private fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { /* no-op callback */ }
        }
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
