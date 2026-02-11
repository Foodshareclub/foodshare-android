package com.foodshare.core.auth

import com.foodshare.core.validation.ValidationBridge

/**
 * Auth-specific validation bridge extending core ValidationBridge.
 * Provides MFA code validation and session validation for auth flows.
 */
object AuthValidationBridge {

    fun validateMFACode(code: String): String? {
        if (code.isBlank()) return "Verification code is required"
        if (code.length != 6) return "Code must be 6 digits"
        if (!code.all { it.isDigit() }) return "Code must contain only digits"
        return null
    }

    fun validateRecoveryCode(code: String): String? {
        if (code.isBlank()) return "Recovery code is required"
        val cleaned = code.replace("-", "").replace(" ", "")
        if (cleaned.length < 8) return "Invalid recovery code format"
        return null
    }

    fun validatePIN(pin: String): String? {
        if (pin.isBlank()) return "PIN is required"
        if (pin.length != 4) return "PIN must be 4 digits"
        if (!pin.all { it.isDigit() }) return "PIN must contain only digits"
        if (pin.toSet().size == 1) return "PIN must not be all the same digit"
        val sequential = "0123456789"
        if (sequential.contains(pin) || sequential.reversed().contains(pin)) {
            return "PIN must not be sequential"
        }
        return null
    }

    fun isSessionExpired(expiresAtEpoch: Long): Boolean {
        return System.currentTimeMillis() / 1000 > expiresAtEpoch
    }

    fun validatePasswordForMFA(password: String): String? {
        return ValidationBridge.validatePasswordWithMessage(password)
    }
}
