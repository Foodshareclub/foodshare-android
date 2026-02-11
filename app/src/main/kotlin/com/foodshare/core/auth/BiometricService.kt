package com.foodshare.core.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BiometricService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun lock() { _isLocked.value = true }
    fun unlock() { _isLocked.value = false }

    suspend fun authenticate(activity: FragmentActivity): Result<Unit> {
        return suspendCancellableCoroutine { cont ->
            val executor = ContextCompat.getMainExecutor(context)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    _isLocked.value = false
                    cont.resume(Result.success(Unit))
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    cont.resume(Result.failure(Exception(errString.toString())))
                }
                override fun onAuthenticationFailed() {
                    // Don't resume - user can retry
                }
            }
            val prompt = BiometricPrompt(activity, executor, callback)
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock FoodShare")
                .setSubtitle("Use your fingerprint or face to unlock")
                .setNegativeButtonText("Use PIN")
                .build()
            prompt.authenticate(info)
        }
    }
}
