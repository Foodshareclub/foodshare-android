package com.foodshare.features.reports.domain.repository

import com.foodshare.features.reports.domain.model.CreateReportInput
import com.foodshare.features.reports.domain.model.Report

interface ReportRepository {
    suspend fun submitReport(input: CreateReportInput): Result<Report>
    suspend fun hasUserReportedPost(postId: Int, userId: String): Result<Boolean>
}
