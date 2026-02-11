package com.foodshare.core.newsletter

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing email notification preferences via Supabase.
 *
 * Controls which email notifications a user receives beyond the newsletter.
 * These preferences govern transactional and system email delivery.
 * Preferences are stored in the `email_preferences` table.
 *
 * SYNC: Mirrors Swift EmailPreferencesService
 */
@Singleton
class EmailPreferencesService @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    companion object {
        private const val TAG = "EmailPreferencesService"
        private const val TABLE_NAME = "email_preferences"
    }

    // ========================================================================
    // Get Preferences
    // ========================================================================

    /**
     * Get the current user's email preferences.
     *
     * Returns default preferences if none have been set yet.
     *
     * @return Result containing the email preferences
     */
    suspend fun getPreferences(): Result<EmailPreferences> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            val response = supabaseClient.from(TABLE_NAME)
                .select {
                    filter { eq("user_id", userId) }
                    limit(1)
                }
                .decodeSingleOrNull<EmailPreferencesResponse>()

            response?.toPreferences() ?: EmailPreferences()
        }
    }

    // ========================================================================
    // Update Preferences
    // ========================================================================

    /**
     * Update the current user's email preferences.
     *
     * Creates the preferences record if it does not exist (upsert).
     *
     * @param preferences The updated email preferences
     * @return Result indicating success or failure
     */
    suspend fun updatePreferences(preferences: EmailPreferences): Result<Unit> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            val request = EmailPreferencesRequest(
                userId = userId,
                messagesEmail = preferences.messagesEmail,
                arrangementUpdates = preferences.arrangementUpdates,
                reviewNotifications = preferences.reviewNotifications,
                listingExpiry = preferences.listingExpiry,
                accountSecurity = preferences.accountSecurity,
                promotionalEmails = preferences.promotionalEmails,
                surveyInvitations = preferences.surveyInvitations,
                monthlyReport = preferences.monthlyReport,
                unsubscribeAll = preferences.unsubscribeAll
            )

            supabaseClient.from(TABLE_NAME)
                .upsert(request) {
                    onConflict = "user_id"
                }

            Log.d(TAG, "Successfully updated email preferences")
        }
    }

    /**
     * Update a single email preference by key.
     *
     * @param key The preference key (e.g., "messages_email", "promotional_emails")
     * @param enabled Whether the preference is enabled
     * @return Result indicating success or failure
     */
    suspend fun updateSinglePreference(key: String, enabled: Boolean): Result<Unit> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            // Validate the key is a known preference
            val validKeys = setOf(
                "messages_email", "arrangement_updates", "review_notifications",
                "listing_expiry", "account_security", "promotional_emails",
                "survey_invitations", "monthly_report", "unsubscribe_all"
            )

            if (key !in validKeys) {
                throw IllegalArgumentException("Unknown preference key: $key")
            }

            supabaseClient.from(TABLE_NAME)
                .update(mapOf(key to enabled)) {
                    filter { eq("user_id", userId) }
                }

            Log.d(TAG, "Updated email preference '$key' to $enabled")
        }
    }

    /**
     * Unsubscribe from all emails.
     *
     * Sets the `unsubscribe_all` flag, which overrides all individual preferences.
     * Account security emails are still sent regardless of this setting.
     *
     * @return Result indicating success or failure
     */
    suspend fun unsubscribeAll(): Result<Unit> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            supabaseClient.from(TABLE_NAME)
                .upsert(
                    mapOf(
                        "user_id" to userId,
                        "unsubscribe_all" to true
                    )
                ) {
                    onConflict = "user_id"
                }

            Log.d(TAG, "Unsubscribed from all emails")
        }
    }

    /**
     * Re-subscribe to emails with default preferences.
     *
     * Clears the `unsubscribe_all` flag and resets to defaults.
     *
     * @return Result indicating success or failure
     */
    suspend fun resubscribe(): Result<Unit> {
        val defaults = EmailPreferences()
        return updatePreferences(defaults)
    }
}

// ============================================================================
// Data Models
// ============================================================================

/**
 * Email notification preferences.
 *
 * Each field controls a category of email notifications.
 * When `unsubscribeAll` is true, all emails except account security are suppressed.
 */
data class EmailPreferences(
    val messagesEmail: Boolean = true,
    val arrangementUpdates: Boolean = true,
    val reviewNotifications: Boolean = true,
    val listingExpiry: Boolean = true,
    val accountSecurity: Boolean = true,
    val promotionalEmails: Boolean = false,
    val surveyInvitations: Boolean = false,
    val monthlyReport: Boolean = true,
    val unsubscribeAll: Boolean = false
) {
    /**
     * Whether any non-essential email is enabled.
     */
    val hasAnyEnabled: Boolean
        get() = !unsubscribeAll && (messagesEmail || arrangementUpdates ||
            reviewNotifications || listingExpiry || promotionalEmails ||
            surveyInvitations || monthlyReport)
}

/**
 * Request model for updating email preferences.
 */
@Serializable
internal data class EmailPreferencesRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("messages_email") val messagesEmail: Boolean,
    @SerialName("arrangement_updates") val arrangementUpdates: Boolean,
    @SerialName("review_notifications") val reviewNotifications: Boolean,
    @SerialName("listing_expiry") val listingExpiry: Boolean,
    @SerialName("account_security") val accountSecurity: Boolean,
    @SerialName("promotional_emails") val promotionalEmails: Boolean,
    @SerialName("survey_invitations") val surveyInvitations: Boolean,
    @SerialName("monthly_report") val monthlyReport: Boolean,
    @SerialName("unsubscribe_all") val unsubscribeAll: Boolean
)

/**
 * Response model from the email_preferences table.
 */
@Serializable
internal data class EmailPreferencesResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("messages_email") val messagesEmail: Boolean = true,
    @SerialName("arrangement_updates") val arrangementUpdates: Boolean = true,
    @SerialName("review_notifications") val reviewNotifications: Boolean = true,
    @SerialName("listing_expiry") val listingExpiry: Boolean = true,
    @SerialName("account_security") val accountSecurity: Boolean = true,
    @SerialName("promotional_emails") val promotionalEmails: Boolean = false,
    @SerialName("survey_invitations") val surveyInvitations: Boolean = false,
    @SerialName("monthly_report") val monthlyReport: Boolean = true,
    @SerialName("unsubscribe_all") val unsubscribeAll: Boolean = false
) {
    fun toPreferences(): EmailPreferences = EmailPreferences(
        messagesEmail = messagesEmail,
        arrangementUpdates = arrangementUpdates,
        reviewNotifications = reviewNotifications,
        listingExpiry = listingExpiry,
        accountSecurity = accountSecurity,
        promotionalEmails = promotionalEmails,
        surveyInvitations = surveyInvitations,
        monthlyReport = monthlyReport,
        unsubscribeAll = unsubscribeAll
    )
}
