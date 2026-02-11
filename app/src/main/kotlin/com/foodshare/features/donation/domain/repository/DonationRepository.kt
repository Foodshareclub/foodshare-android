package com.foodshare.features.donation.domain.repository

import com.foodshare.features.donation.domain.model.Donation
import com.foodshare.features.donation.domain.model.DonationStatus

interface DonationRepository {
    suspend fun createDonation(donation: Donation): Result<Donation>
    suspend fun getDonations(userId: String): Result<List<Donation>>
    suspend fun updateDonationStatus(id: String, status: DonationStatus): Result<Donation>
}
