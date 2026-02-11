package com.foodshare.features.donation.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Donation(
    val id: String,
    @SerialName("donor_id") val donorId: String,
    @SerialName("recipient_id") val recipientId: String? = null,
    @SerialName("listing_id") val listingId: Int? = null,
    val amount: Double? = null,
    val currency: String = "GBP",
    @SerialName("donation_type") val donationType: DonationType = DonationType.FOOD,
    val status: DonationStatus = DonationStatus.PENDING,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
enum class DonationType {
    @SerialName("food") FOOD,
    @SerialName("monetary") MONETARY,
    @SerialName("supplies") SUPPLIES,
    @SerialName("volunteer_time") VOLUNTEER_TIME
}

@Serializable
enum class DonationStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED
}
