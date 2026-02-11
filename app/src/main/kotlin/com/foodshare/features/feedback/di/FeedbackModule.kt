package com.foodshare.features.feedback.di

import com.foodshare.features.feedback.data.SupabaseFeedbackRepository
import com.foodshare.features.feedback.domain.repository.FeedbackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedbackModule {

    @Provides
    @Singleton
    fun provideFeedbackRepository(
        supabaseClient: SupabaseClient
    ): FeedbackRepository {
        return SupabaseFeedbackRepository(supabaseClient)
    }
}
