package com.foodshare.core.cache

import android.content.Context
import android.util.Log
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
import java.util.concurrent.TimeUnit

/**
 * WorkManager CoroutineWorker that runs cache warming periodically.
 *
 * Schedules cache warming every 4 hours to keep frequently accessed
 * data fresh in the offline cache. This ensures that when the user
 * opens the app, they see recent data immediately rather than stale
 * cached content or loading spinners.
 *
 * Constraints:
 * - Requires network connectivity
 * - Requires battery not low (to avoid draining battery)
 *
 * SYNC: Mirrors Swift background app refresh for cache warming
 */
@HiltWorker
class CacheWarmingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cacheWarmingService: CacheWarmingService
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CacheWarmingWorker"
        private const val UNIQUE_WORK_NAME = "cache_warming_periodic"
        private const val INTERVAL_HOURS = 4L
        private const val MAX_ATTEMPTS = 3

        /**
         * Schedule periodic cache warming.
         *
         * Call this once during app initialization (e.g., in Application.onCreate).
         * WorkManager will handle deduplication with KEEP policy.
         *
         * @param context Application context
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<CacheWarmingWorker>(
                INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    2, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )

            Log.d(TAG, "Scheduled periodic cache warming every $INTERVAL_HOURS hours")
        }

        /**
         * Cancel periodic cache warming.
         *
         * @param context Application context
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "Cancelled periodic cache warming")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting cache warming work (attempt ${runAttemptCount + 1})")

        return try {
            val warmingResult = cacheWarmingService.warmAll()

            if (warmingResult.allSucceeded) {
                Log.d(TAG, "Cache warming completed successfully in ${warmingResult.durationMs}ms")
                Result.success()
            } else if (warmingResult.successCount > 0) {
                // Partial success - consider it good enough
                Log.d(TAG, "Cache warming partially completed: " +
                    "${warmingResult.successCount}/4 succeeded in ${warmingResult.durationMs}ms")
                Result.success()
            } else {
                // All tasks failed
                Log.w(TAG, "Cache warming failed completely")
                if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cache warming worker exception", e)
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }
}
