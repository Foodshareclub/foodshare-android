package com.foodshare.features.feedback.data

import com.foodshare.features.feedback.domain.model.CreateFeedbackInput
import com.foodshare.features.feedback.domain.model.Feedback
import com.foodshare.features.feedback.domain.repository.FeedbackRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseFeedbackRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : FeedbackRepository {

    override suspend fun submitFeedback(input: CreateFeedbackInput): Result<Feedback> {
        return runCatching {
            supabaseClient.from("feedback")
                .insert(input) { select() }
                .decodeSingle<Feedback>()
        }
    }
}
