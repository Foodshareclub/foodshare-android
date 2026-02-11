package com.foodshare.features.feedback.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class FeedbackType(
    val displayName: String,
    val icon: ImageVector
) {
    GENERAL("General", Icons.Default.Feedback),
    BUG("Bug Report", Icons.Default.BugReport),
    FEATURE("Feature Request", Icons.Default.Lightbulb),
    COMPLAINT("Complaint", Icons.Default.SentimentDissatisfied);

    companion object {
        fun fromString(value: String): FeedbackType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: GENERAL
    }
}

@Serializable
enum class FeedbackStatus {
    @SerialName("new") NEW,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("resolved") RESOLVED,
    @SerialName("closed") CLOSED
}

@Serializable
data class Feedback(
    val id: Int? = null,
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
    val type: String = "general",
    val status: String = "new",
    @SerialName("user_id") val userId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreateFeedbackInput(
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
    val type: String = "general",
    @SerialName("user_id") val userId: String? = null
)
