package com.foodshare.features.admin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Enums

enum class UserStatusFilter { ALL, ACTIVE, BANNED, SUSPENDED }
enum class AdminUserSortOption { NEWEST, OLDEST, NAME, ROLE }
enum class ModerationStatus { PENDING, REVIEWING, RESOLVED, DISMISSED }
enum class ModerationContentType { POST, COMMENT, MESSAGE, PROFILE }
enum class ModerationPriority { LOW, MEDIUM, HIGH, CRITICAL }
enum class ModerationResolution { APPROVED, REMOVED, WARNING_ISSUED, USER_BANNED, DISMISSED }

@Serializable
enum class AdminAction {
    @SerialName("ban_user") BAN_USER,
    @SerialName("unban_user") UNBAN_USER,
    @SerialName("assign_role") ASSIGN_ROLE,
    @SerialName("revoke_role") REVOKE_ROLE,
    @SerialName("delete_post") DELETE_POST,
    @SerialName("restore_post") RESTORE_POST,
    @SerialName("delete_comment") DELETE_COMMENT,
    @SerialName("resolve_moderation") RESOLVE_MODERATION,
    @SerialName("update_user_status") UPDATE_USER_STATUS
}

@Serializable
enum class AdminResourceType {
    @SerialName("user") USER,
    @SerialName("post") POST,
    @SerialName("comment") COMMENT,
    @SerialName("moderation") MODERATION,
    @SerialName("role") ROLE
}

// Data classes

@Serializable
data class Role(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UserRole(
    val id: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("role_id") val roleId: Int,
    @SerialName("assigned_by") val assignedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminUserProfile(
    val id: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("ban_reason") val banReason: String? = null,
    @SerialName("banned_at") val bannedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_sign_in") val lastSignIn: String? = null,
    val roles: List<Role> = emptyList()
) {
    val initials: String
        get() = (displayName ?: email ?: "U")
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")

    val statusDisplay: String
        get() = when {
            isBanned -> "Banned"
            else -> "Active"
        }
}

@Serializable
data class AdminAuditLog(
    val id: Int,
    @SerialName("admin_id") val adminId: String,
    @SerialName("admin_name") val adminName: String? = null,
    val action: String,
    @SerialName("resource_type") val resourceType: String,
    @SerialName("resource_id") val resourceId: String? = null,
    val details: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ModerationQueueItem(
    val id: Int,
    @SerialName("content_type") val contentType: String,
    @SerialName("content_id") val contentId: Int,
    @SerialName("reported_by") val reportedBy: String? = null,
    @SerialName("reporter_name") val reporterName: String? = null,
    val reason: String? = null,
    val description: String? = null,
    val status: String = "pending",
    val priority: String = "medium",
    @SerialName("content_preview") val contentPreview: String? = null,
    @SerialName("content_author_id") val contentAuthorId: String? = null,
    @SerialName("content_author_name") val contentAuthorName: String? = null,
    val resolution: String? = null,
    @SerialName("resolved_by") val resolvedBy: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("resolution_notes") val resolutionNotes: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    val priorityEnum: ModerationPriority
        get() = ModerationPriority.entries.find {
            it.name.equals(priority, ignoreCase = true)
        } ?: ModerationPriority.MEDIUM

    val statusEnum: ModerationStatus
        get() = ModerationStatus.entries.find {
            it.name.equals(status, ignoreCase = true)
        } ?: ModerationStatus.PENDING
}

@Serializable
data class AdminDashboardStats(
    @SerialName("total_users") val totalUsers: Int = 0,
    @SerialName("active_users") val activeUsers: Int = 0,
    @SerialName("banned_users") val bannedUsers: Int = 0,
    @SerialName("total_posts") val totalPosts: Int = 0,
    @SerialName("active_posts") val activePosts: Int = 0,
    @SerialName("pending_reports") val pendingReports: Int = 0,
    @SerialName("resolved_today") val resolvedToday: Int = 0,
    @SerialName("new_users_today") val newUsersToday: Int = 0
)

data class AdminUserFilters(
    val query: String = "",
    val statusFilter: UserStatusFilter = UserStatusFilter.ALL,
    val roleFilter: String? = null,
    val sortOption: AdminUserSortOption = AdminUserSortOption.NEWEST
)

data class ModerationFilters(
    val status: ModerationStatus? = null,
    val contentType: ModerationContentType? = null,
    val priority: ModerationPriority? = null
)
