package com.foodshare.features.arrangement.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Arrangement(
    val id: String,
    @SerialName("listing_id") val listingId: Int,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("owner_id") val ownerId: String,
    val status: ArrangementStatus = ArrangementStatus.PENDING,
    @SerialName("pickup_date") val pickupDate: String? = null,
    @SerialName("pickup_time") val pickupTime: String? = null,
    @SerialName("pickup_location") val pickupLocation: String? = null,
    @SerialName("pickup_latitude") val pickupLatitude: Double? = null,
    @SerialName("pickup_longitude") val pickupLongitude: Double? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("listing_title") val listingTitle: String? = null,
    @SerialName("requester_name") val requesterName: String? = null,
    @SerialName("owner_name") val ownerName: String? = null
)

@Serializable
enum class ArrangementStatus {
    @SerialName("pending") PENDING,
    @SerialName("accepted") ACCEPTED,
    @SerialName("declined") DECLINED,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("no_show") NO_SHOW
}
