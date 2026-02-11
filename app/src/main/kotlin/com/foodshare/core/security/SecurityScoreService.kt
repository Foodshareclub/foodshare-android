package com.foodshare.core.security

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security check item with details
 */
data class SecurityCheckItem(
    val title: String,
    val description: String,
    val isPassed: Boolean,
    val points: Int
)

/**
 * Service for calculating user security score
 *
 * Evaluates security best practices and provides a score out of 100
 */
@Singleton
class SecurityScoreService @Inject constructor() {

    /**
     * Calculate security score based on various checks
     *
     * @param emailVerified Whether user's email is verified
     * @param hasMFA Whether user has two-factor authentication enabled
     * @param hasStrongPassword Whether user has a strong password
     * @param profileComplete Whether user profile is complete
     * @return Pair of score (0-100) and list of check items
     */
    fun calculateScore(
        emailVerified: Boolean,
        hasMFA: Boolean,
        hasStrongPassword: Boolean,
        profileComplete: Boolean
    ): Pair<Int, List<SecurityCheckItem>> {
        val items = listOf(
            SecurityCheckItem(
                title = "Email Verified",
                description = "Confirm your email address",
                isPassed = emailVerified,
                points = 25
            ),
            SecurityCheckItem(
                title = "Strong Password",
                description = "Use a strong password",
                isPassed = hasStrongPassword,
                points = 25
            ),
            SecurityCheckItem(
                title = "Two-Factor Auth",
                description = "Enable MFA for extra security",
                isPassed = hasMFA,
                points = 30
            ),
            SecurityCheckItem(
                title = "Complete Profile",
                description = "Fill in your profile details",
                isPassed = profileComplete,
                points = 20
            )
        )

        val score = items.filter { it.isPassed }.sumOf { it.points }
        return Pair(score, items)
    }

    /**
     * Get security level based on score
     */
    fun getSecurityLevel(score: Int): SecurityLevel {
        return when {
            score >= 80 -> SecurityLevel.EXCELLENT
            score >= 60 -> SecurityLevel.GOOD
            score >= 40 -> SecurityLevel.FAIR
            else -> SecurityLevel.POOR
        }
    }
}

/**
 * Security level enum
 */
enum class SecurityLevel(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor")
}
