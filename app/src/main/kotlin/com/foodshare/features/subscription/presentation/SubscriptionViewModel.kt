package com.foodshare.features.subscription.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.subscription.domain.model.PricingInfo
import com.foodshare.features.subscription.domain.model.PurchaseResult
import com.foodshare.features.subscription.domain.model.SubscriptionPlan
import com.foodshare.features.subscription.domain.model.SubscriptionPeriod
import com.foodshare.features.subscription.domain.model.SubscriptionStatus
import com.foodshare.features.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlan: SubscriptionPlan? = null,
    val currentStatus: SubscriptionStatus = SubscriptionStatus.NONE,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val error: String? = null
) {
    val pricing: PricingInfo
        get() = PricingInfo(
            monthlyPlan = plans.find { it.period == SubscriptionPeriod.MONTHLY },
            yearlyPlan = plans.find { it.period == SubscriptionPeriod.YEARLY }
        )

    val isSubscribed: Boolean
        get() = currentStatus.isActive
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        checkSubscriptionStatus()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getProducts()
                .onSuccess { plans ->
                    // Default select yearly
                    val defaultPlan = plans.find { it.isYearly } ?: plans.firstOrNull()
                    _uiState.update {
                        it.copy(
                            plans = plans,
                            selectedPlan = defaultPlan,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load plans"
                        )
                    }
                }
        }
    }

    private fun checkSubscriptionStatus() {
        viewModelScope.launch {
            val status = repository.getSubscriptionStatus()
            _uiState.update { it.copy(currentStatus = status) }
        }
    }

    fun selectPlan(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedPlan = plan) }
    }

    fun purchase(activity: Activity) {
        val plan = _uiState.value.selectedPlan ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, error = null) }

            val result = repository.purchase(activity, plan)

            when (result) {
                is PurchaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseSuccess = true,
                            currentStatus = SubscriptionStatus.ACTIVE
                        )
                    }
                }
                is PurchaseResult.Cancelled -> {
                    _uiState.update { it.copy(isPurchasing = false) }
                }
                is PurchaseResult.Error -> {
                    _uiState.update {
                        it.copy(isPurchasing = false, error = result.message)
                    }
                }
                is PurchaseResult.Pending -> {
                    _uiState.update {
                        it.copy(isPurchasing = false, error = "Purchase is pending approval")
                    }
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null) }
            val status = repository.restorePurchases()
            _uiState.update {
                it.copy(
                    isRestoring = false,
                    currentStatus = status,
                    error = if (status == SubscriptionStatus.NONE) "No active subscription found" else null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
