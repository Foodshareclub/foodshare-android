package com.foodshare.features.admin.di

import com.foodshare.features.admin.data.SupabaseAdminRepository
import com.foodshare.features.admin.domain.repository.AdminRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminModule {

    @Provides
    @Singleton
    fun provideAdminRepository(
        supabaseClient: SupabaseClient
    ): AdminRepository {
        return SupabaseAdminRepository(supabaseClient)
    }
}
