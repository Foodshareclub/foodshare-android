package com.foodshare.core.invitation

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing a sent invitation.
 */
@Serializable
data class SentInvite(
    val id: String,
    val email: String,
    val status: String, // "sent", "accepted", "expired"
    @SerialName("sent_at") val sentAt: String,
    @SerialName("referral_code") val referralCode: String? = null,
    val message: String? = null
)

/**
 * Service for managing user invitations and referrals.
 *
 * Handles sending invitations via email, tracking invitation history,
 * and generating referral links. Backed by the Supabase "invitations" table.
 */
@Singleton
class InvitationService @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    /**
     * Send an invitation to the specified email address.
     *
     * @param email The recipient's email address
     * @param message An optional personal message to include
     * @param referralCode The sender's referral code
     * @return Result indicating success or failure
     */
    suspend fun sendInvitation(email: String, message: String?, referralCode: String): Result<Unit> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            supabaseClient.from("invitations")
                .insert(
                    mapOf(
                        "sender_id" to userId,
                        "email" to email,
                        "message" to (message ?: ""),
                        "referral_code" to referralCode,
                        "status" to "sent"
                    )
                )
        }
    }

    /**
     * Retrieve the invitation history for the current user.
     *
     * @return Result containing the list of sent invitations, ordered by most recent first
     */
    suspend fun getInvitationHistory(): Result<List<SentInvite>> {
        return runCatching {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated")

            supabaseClient.from("invitations")
                .select {
                    filter { eq("sender_id", userId) }
                    order("sent_at", Order.DESCENDING)
                }
                .decodeList<SentInvite>()
        }
    }

    /**
     * Generate a shareable referral link from a referral code.
     *
     * @param code The referral code to embed in the link
     * @return The full referral URL
     */
    fun generateReferralLink(code: String): String = "https://foodshare.club/invite/$code"
}
