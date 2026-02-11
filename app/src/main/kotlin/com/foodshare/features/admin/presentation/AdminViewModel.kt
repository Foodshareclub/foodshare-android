package com.foodshare.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.admin.domain.model.AdminAuditLog
import com.foodshare.features.admin.domain.model.AdminDashboardStats
import com.foodshare.features.admin.domain.model.AdminUserFilters
import com.foodshare.features.admin.domain.model.AdminUserProfile
import com.foodshare.features.admin.domain.model.ModerationQueueItem
import com.foodshare.features.admin.domain.model.ModerationResolution
import com.foodshare.features.admin.domain.model.ModerationStatus
import com.foodshare.features.admin.domain.model.Role
import com.foodshare.features.admin.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AdminTab {
    data object Dashboard : AdminTab
    data object Users : AdminTab
    data object Moderation : AdminTab
    data object AuditLog : AdminTab
}

data class AdminUiState(
    val currentTab: AdminTab = AdminTab.Dashboard,
    val isLoading: Boolean = true,
    val error: String? = null,

    // Dashboard
    val stats: AdminDashboardStats = AdminDashboardStats(),

    // Users
    val users: List<AdminUserProfile> = emptyList(),
    val userFilters: AdminUserFilters = AdminUserFilters(),
    val isLoadingUsers: Boolean = false,
    val selectedUser: AdminUserProfile? = null,
    val roles: List<Role> = emptyList(),

    // Moderation
    val moderationQueue: List<ModerationQueueItem> = emptyList(),
    val isLoadingModeration: Boolean = false,
    val selectedModerationItem: ModerationQueueItem? = null,
    val moderationStatusFilter: ModerationStatus? = null,

    // Audit
    val auditLogs: List<AdminAuditLog> = emptyList(),
    val isLoadingAudit: Boolean = false,

    // Admin access
    val hasAccess: Boolean = false,
    val isSuperAdmin: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        checkAccess()
    }

    private fun checkAccess() {
        viewModelScope.launch {
            val hasAccess = repository.hasAdminAccess()
            val isSuperAdmin = if (hasAccess) repository.hasSuperAdminAccess() else false
            _uiState.update {
                it.copy(hasAccess = hasAccess, isSuperAdmin = isSuperAdmin, isLoading = false)
            }
            if (hasAccess) {
                loadDashboard()
            }
        }
    }

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(currentTab = tab) }
        when (tab) {
            AdminTab.Dashboard -> loadDashboard()
            AdminTab.Users -> loadUsers()
            AdminTab.Moderation -> loadModerationQueue()
            AdminTab.AuditLog -> loadAuditLogs()
        }
    }

    // Dashboard

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.fetchDashboardStats()
                .onSuccess { stats ->
                    _uiState.update { it.copy(stats = stats, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    // Users

    fun loadUsers() {
        viewModelScope.launch {
            val filters = _uiState.value.userFilters
            _uiState.update { it.copy(isLoadingUsers = true) }
            repository.fetchUsers(
                query = filters.query,
                role = filters.roleFilter
            )
                .onSuccess { users ->
                    _uiState.update { it.copy(users = users, isLoadingUsers = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingUsers = false) }
                }
        }
    }

    fun updateUserSearch(query: String) {
        _uiState.update { it.copy(userFilters = it.userFilters.copy(query = query)) }
        loadUsers()
    }

    fun selectUser(user: AdminUserProfile?) {
        _uiState.update { it.copy(selectedUser = user) }
        if (user != null && _uiState.value.roles.isEmpty()) {
            loadRoles()
        }
    }

    fun banUser(userId: String, reason: String) {
        viewModelScope.launch {
            repository.banUser(userId, reason)
                .onSuccess {
                    selectUser(null)
                    loadUsers()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun unbanUser(userId: String) {
        viewModelScope.launch {
            repository.unbanUser(userId)
                .onSuccess {
                    selectUser(null)
                    loadUsers()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    // Roles

    private fun loadRoles() {
        viewModelScope.launch {
            repository.fetchRoles()
                .onSuccess { roles ->
                    _uiState.update { it.copy(roles = roles) }
                }
        }
    }

    fun assignRole(userId: String, roleId: Int) {
        viewModelScope.launch {
            repository.assignRole(userId, roleId)
                .onSuccess { loadUsers() }
                .onFailure { error -> _uiState.update { it.copy(error = error.message) } }
        }
    }

    fun revokeRole(userId: String, roleId: Int) {
        viewModelScope.launch {
            repository.revokeRole(userId, roleId)
                .onSuccess { loadUsers() }
                .onFailure { error -> _uiState.update { it.copy(error = error.message) } }
        }
    }

    // Moderation

    fun loadModerationQueue() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModeration = true) }
            val statusFilter = _uiState.value.moderationStatusFilter?.name?.lowercase()
            repository.fetchModerationQueue(status = statusFilter)
                .onSuccess { queue ->
                    _uiState.update { it.copy(moderationQueue = queue, isLoadingModeration = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingModeration = false) }
                }
        }
    }

    fun setModerationFilter(status: ModerationStatus?) {
        _uiState.update { it.copy(moderationStatusFilter = status) }
        loadModerationQueue()
    }

    fun selectModerationItem(item: ModerationQueueItem?) {
        _uiState.update { it.copy(selectedModerationItem = item) }
    }

    fun resolveModerationItem(itemId: Int, resolution: ModerationResolution, notes: String) {
        viewModelScope.launch {
            repository.resolveModerationItem(itemId, resolution, notes)
                .onSuccess {
                    selectModerationItem(null)
                    loadModerationQueue()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    // Content

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            repository.deletePost(postId)
                .onSuccess { loadModerationQueue() }
                .onFailure { error -> _uiState.update { it.copy(error = error.message) } }
        }
    }

    // Audit

    fun loadAuditLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAudit = true) }
            repository.fetchAuditLogs()
                .onSuccess { logs ->
                    _uiState.update { it.copy(auditLogs = logs, isLoadingAudit = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingAudit = false) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
