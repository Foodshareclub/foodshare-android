package com.foodshare.features.reports.di

import com.foodshare.features.reports.data.SupabaseReportRepository
import com.foodshare.features.reports.domain.repository.ReportRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportModule {

    @Provides
    @Singleton
    fun provideReportRepository(
        supabaseClient: SupabaseClient
    ): ReportRepository {
        return SupabaseReportRepository(supabaseClient)
    }
}
