package com.foodshare.features.arrangement.di

import com.foodshare.core.realtime.RealtimeChannelManager
import com.foodshare.features.arrangement.data.repository.SupabaseArrangementRepository
import com.foodshare.features.arrangement.domain.repository.ArrangementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

/**
 * Hilt module for Arrangement feature dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ArrangementModule {

    @Provides
    @Singleton
    fun provideArrangementRepository(
        supabaseClient: SupabaseClient,
        realtimeManager: RealtimeChannelManager
    ): ArrangementRepository {
        return SupabaseArrangementRepository(supabaseClient, realtimeManager)
    }
}
