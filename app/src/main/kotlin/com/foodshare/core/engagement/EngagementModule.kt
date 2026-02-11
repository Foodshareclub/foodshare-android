package com.foodshare.core.engagement

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

/**
 * Hilt module providing engagement tracking dependencies.
 *
 * Installs into SingletonComponent to ensure a single EngagementTracker
 * instance is shared across the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object EngagementModule {

    /**
     * Provides the EngagementTracker singleton.
     *
     * The tracker batches engagement events and periodically flushes them
     * to Supabase. It should be started/stopped with the app lifecycle.
     */
    @Provides
    @Singleton
    fun provideEngagementTracker(
        supabaseClient: SupabaseClient,
        @ApplicationContext context: Context
    ): EngagementTracker {
        return EngagementTracker(supabaseClient, context)
    }
}
