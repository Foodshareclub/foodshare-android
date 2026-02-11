package com.foodshare.features.donation.data.repository

import com.foodshare.features.donation.domain.model.Donation
import com.foodshare.features.donation.domain.model.DonationStatus
import com.foodshare.features.donation.domain.model.DonationType
import com.foodshare.features.donation.domain.repository.DonationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase implementation of DonationRepository.
 *
 * Manages donation creation, retrieval, and status updates.
 */
@Singleton
class SupabaseDonationRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DonationRepository {

    private val currentUserId: String?
        get() = supabaseClient.auth.currentUserOrNull()?.id

    override suspend fun createDonation(donation: Donation): Result<Donation> {
        val userId = currentUserId ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )

        return runCatching {
            supabaseClient.from("donations")
                .insert(
                    mapOf(
                        "donor_id" to userId,
                        "recipient_id" to donation.recipientId,
                        "listing_id" to donation.listingId,
                        "amount" to donation.amount,
                        "currency" to donation.currency,
                        "donation_type" to donation.donationType.name.lowercase(),
                        "status" to donation.status.name.lowercase(),
                        "notes" to donation.notes
                    )
                ) {
                    select()
                }
                .decodeSingle<Donation>()
        }
    }

    override suspend fun getDonations(userId: String): Result<List<Donation>> {
        return runCatching {
            supabaseClient.from("donations")
                .select {
                    filter { eq("donor_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Donation>()
        }
    }

    override suspend fun updateDonationStatus(id: String, status: DonationStatus): Result<Donation> {
        return runCatching {
            supabaseClient.from("donations")
                .update(
                    mapOf("status" to status.name.lowercase())
                ) {
                    filter { eq("id", id) }
                    select()
                }
                .decodeSingle<Donation>()
        }
    }
}
