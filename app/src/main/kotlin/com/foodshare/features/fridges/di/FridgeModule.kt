package com.foodshare.features.fridges.di

import com.foodshare.features.fridges.data.repository.SupabaseFridgeRepository
import com.foodshare.features.fridges.domain.repository.FridgeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class FridgeModule {

    @Binds
    @ActivityScoped
    abstract fun bindFridgeRepository(
        impl: SupabaseFridgeRepository
    ): FridgeRepository
}
