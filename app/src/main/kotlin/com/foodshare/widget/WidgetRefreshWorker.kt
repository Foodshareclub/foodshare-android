package com.foodshare.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that refreshes widget data every 30 minutes.
 *
 * Fetches fresh data from Supabase for all widget types:
 * - Nearby food listings
 * - User impact stats
 * - Active challenge progress
 *
 * After refreshing data, triggers a UI update on all widget instances
 * so they display the latest information.
 *
 * Constraints:
 * - Requires network connectivity
 * - Respects battery optimization
 *
 * SYNC: Mirrors Swift WidgetKit timeline refresh
 */
@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val supabaseClient: SupabaseClient
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WidgetRefreshWorker"
        private const val UNIQUE_WORK_NAME = "widget_data_refresh"
        private const val REFRESH_INTERVAL_MINUTES = 30L
        private const val MAX_ATTEMPTS = 3

        /**
         * Schedule periodic widget data refresh.
         *
         * Call this once during app initialization. WorkManager
         * deduplicates with KEEP policy, so it is safe to call
         * multiple times.
         *
         * @param context Application context
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
                REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )

            Log.d(TAG, "Scheduled widget refresh every $REFRESH_INTERVAL_MINUTES minutes")
        }

        /**
         * Cancel periodic widget data refresh.
         *
         * @param context Application context
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "Cancelled periodic widget refresh")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting widget data refresh (attempt ${runAttemptCount + 1})")

        return try {
            // Refresh all widget data
            WidgetDataService.refreshAll(context, supabaseClient)

            // Trigger widget UI updates
            updateWidgets()

            Log.d(TAG, "Widget data refresh completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Widget data refresh failed: ${e.message}", e)
            if (runAttemptCount < MAX_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Trigger UI updates on all widget instances.
     *
     * Each widget type is updated independently. Failures updating
     * one widget type do not prevent others from updating.
     */
    private suspend fun updateWidgets() {
        try {
            NearbyFoodWidget().updateAll(context)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update NearbyFoodWidget: ${e.message}")
        }

        try {
            StatsWidget().updateAll(context)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update StatsWidget: ${e.message}")
        }

        try {
            ChallengeWidget().updateAll(context)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update ChallengeWidget: ${e.message}")
        }
    }
}
