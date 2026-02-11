package com.foodshare.features.reports.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ReportReason(
    val displayName: String,
    val icon: ImageVector
) {
    SPAM("Spam", Icons.Default.Block),
    INAPPROPRIATE("Inappropriate", Icons.Default.Report),
    MISLEADING("Misleading", Icons.Default.Error),
    EXPIRED("Expired", Icons.Default.Schedule),
    WRONG_LOCATION("Wrong Location", Icons.Default.LocationOff),
    SAFETY_CONCERN("Safety Concern", Icons.Default.GppBad),
    DUPLICATE("Duplicate", Icons.Default.ContentCopy),
    OTHER("Other", Icons.Default.MoreHoriz);

    companion object {
        fun fromString(value: String): ReportReason =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

@Serializable
enum class ReportStatus {
    @SerialName("pending") PENDING,
    @SerialName("reviewing") REVIEWING,
    @SerialName("resolved") RESOLVED,
    @SerialName("dismissed") DISMISSED
}

@Serializable
data class Report(
    val id: Int,
    @SerialName("post_id") val postId: Int,
    @SerialName("reporter_id") val reporterId: String,
    val reason: String,
    val description: String? = null,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreateReportInput(
    @SerialName("post_id") val postId: Int,
    @SerialName("reporter_id") val reporterId: String,
    val reason: String,
    val description: String? = null
)
