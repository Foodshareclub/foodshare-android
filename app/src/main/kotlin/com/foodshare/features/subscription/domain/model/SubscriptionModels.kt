package com.foodshare.features.subscription.domain.model

enum class SubscriptionStatus {
    ACTIVE, EXPIRED, CANCELLED, NONE;

    val isActive: Boolean get() = this == ACTIVE
}

enum class SubscriptionPeriod(val displayName: String, val months: Int) {
    MONTHLY("Monthly", 1),
    YEARLY("Yearly", 12)
}

data class SubscriptionPlan(
    val productId: String,
    val period: SubscriptionPeriod,
    val price: Long, // micros
    val formattedPrice: String,
    val currencyCode: String = "NZD"
) {
    val isYearly: Boolean get() = period == SubscriptionPeriod.YEARLY
}

data class PricingInfo(
    val monthlyPlan: SubscriptionPlan?,
    val yearlyPlan: SubscriptionPlan?
) {
    val hasSavings: Boolean
        get() {
            val monthly = monthlyPlan ?: return false
            val yearly = yearlyPlan ?: return false
            return yearly.price < monthly.price * 12
        }

    val savingsPercent: Int
        get() {
            val monthly = monthlyPlan ?: return 0
            val yearly = yearlyPlan ?: return 0
            val monthlyAnnual = monthly.price * 12
            if (monthlyAnnual == 0L) return 0
            return ((1 - yearly.price.toDouble() / monthlyAnnual) * 100).toInt()
        }

    val effectiveMonthlyPrice: String
        get() {
            val yearly = yearlyPlan ?: return ""
            val monthlyMicros = yearly.price / 12
            return formatMicros(monthlyMicros, yearly.currencyCode)
        }

    private fun formatMicros(micros: Long, currency: String): String {
        val amount = micros / 1_000_000.0
        return "$${String.format("%.2f", amount)}"
    }
}

sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Error(val message: String) : PurchaseResult
    data object Pending : PurchaseResult
}
