package com.foodshare.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestManager @Inject constructor() {
    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    private val _guestSessionId = MutableStateFlow<String?>(null)
    val guestSessionId: StateFlow<String?> = _guestSessionId.asStateFlow()

    fun startGuestSession() {
        _isGuest.value = true
        _guestSessionId.value = java.util.UUID.randomUUID().toString()
    }

    fun endGuestSession() {
        _isGuest.value = false
        _guestSessionId.value = null
    }

    fun isGuestMode(): Boolean = _isGuest.value
}
