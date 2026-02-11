package com.foodshare.features.insights.di

import com.foodshare.features.insights.data.repository.SupabaseInsightsRepository
import com.foodshare.features.insights.domain.repository.InsightsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class InsightsModule {

    @Binds
    @ActivityScoped
    abstract fun bindInsightsRepository(
        impl: SupabaseInsightsRepository
    ): InsightsRepository
}
