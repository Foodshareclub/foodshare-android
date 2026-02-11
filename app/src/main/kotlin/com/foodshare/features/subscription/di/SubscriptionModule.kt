package com.foodshare.features.subscription.di

import android.content.Context
import com.foodshare.features.subscription.data.BillingService
import com.foodshare.features.subscription.data.PlayBillingRepository
import com.foodshare.features.subscription.domain.repository.SubscriptionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubscriptionModule {

    @Provides
    @Singleton
    fun provideBillingService(
        @ApplicationContext context: Context
    ): BillingService {
        return BillingService(context)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        billingService: BillingService
    ): SubscriptionRepository {
        return PlayBillingRepository(billingService)
    }
}
