package com.foodshare.features.reports.data

import com.foodshare.features.reports.domain.model.CreateReportInput
import com.foodshare.features.reports.domain.model.Report
import com.foodshare.features.reports.domain.repository.ReportRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseReportRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ReportRepository {

    override suspend fun submitReport(input: CreateReportInput): Result<Report> {
        return runCatching {
            supabaseClient.from("post_reports")
                .insert(input) { select() }
                .decodeSingle<Report>()
        }
    }

    override suspend fun hasUserReportedPost(postId: Int, userId: String): Result<Boolean> {
        return runCatching {
            val result = supabaseClient.from("post_reports")
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("reporter_id", userId)
                    }
                    limit(1)
                }
                .decodeList<Report>()
            result.isNotEmpty()
        }
    }
}
