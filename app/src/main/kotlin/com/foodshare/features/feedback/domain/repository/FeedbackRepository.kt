package com.foodshare.features.feedback.domain.repository

import com.foodshare.features.feedback.domain.model.CreateFeedbackInput
import com.foodshare.features.feedback.domain.model.Feedback

interface FeedbackRepository {
    suspend fun submitFeedback(input: CreateFeedbackInput): Result<Feedback>
}
