package com.foodshare.features.admin.domain.repository

import com.foodshare.features.admin.domain.model.AdminAuditLog
import com.foodshare.features.admin.domain.model.AdminDashboardStats
import com.foodshare.features.admin.domain.model.AdminUserProfile
import com.foodshare.features.admin.domain.model.ModerationQueueItem
import com.foodshare.features.admin.domain.model.ModerationResolution
import com.foodshare.features.admin.domain.model.Role

interface AdminRepository {
    // Dashboard
    suspend fun fetchDashboardStats(): Result<AdminDashboardStats>

    // Users
    suspend fun fetchUsers(
        query: String = "",
        role: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<AdminUserProfile>>

    suspend fun fetchUser(id: String): Result<AdminUserProfile>
    suspend fun banUser(userId: String, reason: String): Result<Unit>
    suspend fun unbanUser(userId: String): Result<Unit>

    // Roles
    suspend fun fetchRoles(): Result<List<Role>>
    suspend fun assignRole(userId: String, roleId: Int): Result<Unit>
    suspend fun revokeRole(userId: String, roleId: Int): Result<Unit>

    // Moderation
    suspend fun fetchModerationQueue(
        status: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<ModerationQueueItem>>

    suspend fun resolveModerationItem(
        itemId: Int,
        resolution: ModerationResolution,
        notes: String
    ): Result<Unit>

    // Content
    suspend fun deletePost(postId: Int): Result<Unit>
    suspend fun restorePost(postId: Int): Result<Unit>
    suspend fun deleteComment(commentId: Int): Result<Unit>

    // Audit
    suspend fun fetchAuditLogs(limit: Int = 50, offset: Int = 0): Result<List<AdminAuditLog>>
    suspend fun logAction(
        action: String,
        resourceType: String,
        resourceId: String?,
        details: String?
    ): Result<Unit>

    // Auth
    suspend fun hasAdminAccess(): Boolean
    suspend fun hasSuperAdminAccess(): Boolean
}
