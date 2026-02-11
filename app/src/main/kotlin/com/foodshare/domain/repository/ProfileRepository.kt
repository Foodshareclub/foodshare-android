package com.foodshare.domain.repository

import com.foodshare.domain.model.ProfileStats
import com.foodshare.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val currentProfile: Flow<UserProfile?>
    suspend fun getProfile(userId: String): Result<UserProfile>
    suspend fun updateProfile(nickname: String?, bio: String?, location: String?, avatarUrl: String?): Result<UserProfile>
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String): Result<String>
    suspend fun deleteAvatar(userId: String): Result<Unit>
    suspend fun getStats(userId: String): Result<ProfileStats>
}
