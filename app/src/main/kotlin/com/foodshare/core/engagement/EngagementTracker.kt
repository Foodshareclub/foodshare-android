package com.foodshare.core.engagement

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton service that batches engagement events and writes to Supabase.
 *
 * Features:
 * - Batched event writes for efficiency (reduces network calls)
 * - Periodic flush every 30 seconds
 * - Immediate flush when queue reaches 20 items
 * - Thread-safe queue operations via Mutex
 * - Automatic profile ID injection from current auth session
 *
 * Supported event types:
 * - impression: User saw a piece of content
 * - click: User tapped on content
 * - time_spent: Duration spent viewing content
 * - reaction: User reacted to content (like, love, etc.)
 * - share: User shared content externally
 *
 * SYNC: Mirrors Swift EngagementTracker
 */
@Singleton
class EngagementTracker @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "EngagementTracker"
        private const val TABLE_NAME = "engagement_events"
        private const val FLUSH_INTERVAL_MS = 30_000L // 30 seconds
        private const val MAX_QUEUE_SIZE = 20
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2_000L

        // Event type constants
        const val EVENT_IMPRESSION = "impression"
        const val EVENT_CLICK = "click"
        const val EVENT_TIME_SPENT = "time_spent"
        const val EVENT_REACTION = "reaction"
        const val EVENT_SHARE = "share"
    }

    private val eventQueue = mutableListOf<EngagementEvent>()
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var flushJob: Job? = null
    private var isStarted = false

    // ========================================================================
    // Lifecycle
    // ========================================================================

    /**
     * Start the periodic flush timer.
     * Call this when the app comes to the foreground.
     */
    fun start() {
        if (isStarted) return
        isStarted = true

        flushJob = scope.launch {
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                flush()
            }
        }
        Log.d(TAG, "Engagement tracker started")
    }

    /**
     * Stop the periodic flush timer and flush remaining events.
     * Call this when the app goes to the background.
     */
    fun stop() {
        isStarted = false
        flushJob?.cancel()
        flushJob = null

        // Flush remaining events before stopping
        scope.launch {
            flush()
        }
        Log.d(TAG, "Engagement tracker stopped")
    }

    // ========================================================================
    // Public Tracking Methods
    // ========================================================================

    /**
     * Track an impression event (user saw content).
     *
     * @param contentType The type of content (e.g., "listing", "forum_post", "challenge")
     * @param contentId The unique identifier of the content
     */
    fun trackImpression(contentType: String, contentId: String) {
        enqueue(
            EngagementEvent(
                eventType = EVENT_IMPRESSION,
                contentType = contentType,
                contentId = contentId,
                profileId = getCurrentProfileId(),
                createdAt = nowIso8601()
            )
        )
    }

    /**
     * Track a click event (user tapped on content).
     *
     * @param contentType The type of content
     * @param contentId The unique identifier of the content
     */
    fun trackClick(contentType: String, contentId: String) {
        enqueue(
            EngagementEvent(
                eventType = EVENT_CLICK,
                contentType = contentType,
                contentId = contentId,
                profileId = getCurrentProfileId(),
                createdAt = nowIso8601()
            )
        )
    }

    /**
     * Track time spent viewing content.
     *
     * @param contentType The type of content
     * @param contentId The unique identifier of the content
     * @param durationMs Time spent in milliseconds
     */
    fun trackTimeSpent(contentType: String, contentId: String, durationMs: Long) {
        if (durationMs <= 0) return

        enqueue(
            EngagementEvent(
                eventType = EVENT_TIME_SPENT,
                contentType = contentType,
                contentId = contentId,
                profileId = getCurrentProfileId(),
                metadata = mapOf("duration_ms" to durationMs.toString()),
                createdAt = nowIso8601()
            )
        )
    }

    /**
     * Track a reaction event (user reacted to content).
     *
     * @param contentType The type of content
     * @param contentId The unique identifier of the content
     * @param reactionType The type of reaction (e.g., "like", "love", "helpful")
     */
    fun trackReaction(contentType: String, contentId: String, reactionType: String) {
        enqueue(
            EngagementEvent(
                eventType = EVENT_REACTION,
                contentType = contentType,
                contentId = contentId,
                profileId = getCurrentProfileId(),
                metadata = mapOf("reaction_type" to reactionType),
                createdAt = nowIso8601()
            )
        )
    }

    /**
     * Track a share event (user shared content externally).
     *
     * @param contentType The type of content
     * @param contentId The unique identifier of the content
     */
    fun trackShare(contentType: String, contentId: String) {
        enqueue(
            EngagementEvent(
                eventType = EVENT_SHARE,
                contentType = contentType,
                contentId = contentId,
                profileId = getCurrentProfileId(),
                createdAt = nowIso8601()
            )
        )
    }

    // ========================================================================
    // Queue Management
    // ========================================================================

    /**
     * Add an event to the queue. Triggers an immediate flush if the queue is full.
     */
    private fun enqueue(event: EngagementEvent) {
        scope.launch {
            val shouldFlush: Boolean
            mutex.withLock {
                eventQueue.add(event)
                shouldFlush = eventQueue.size >= MAX_QUEUE_SIZE
            }
            if (shouldFlush) {
                Log.d(TAG, "Queue full ($MAX_QUEUE_SIZE items), flushing immediately")
                flush()
            }
        }
    }

    /**
     * Flush all queued events to Supabase.
     *
     * Drains the queue atomically, then attempts a batch insert.
     * On failure, retries up to MAX_RETRY_ATTEMPTS times with exponential backoff.
     * Events that fail all retries are dropped and logged.
     */
    private suspend fun flush() {
        val eventsToFlush: List<EngagementEvent>
        mutex.withLock {
            if (eventQueue.isEmpty()) return
            eventsToFlush = eventQueue.toList()
            eventQueue.clear()
        }

        Log.d(TAG, "Flushing ${eventsToFlush.size} engagement events")

        var attempt = 0
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                supabaseClient.from(TABLE_NAME)
                    .insert(eventsToFlush)

                Log.d(TAG, "Successfully flushed ${eventsToFlush.size} events")
                return
            } catch (e: Exception) {
                attempt++
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    val backoffMs = RETRY_DELAY_MS * (1 shl (attempt - 1))
                    Log.w(TAG, "Flush failed (attempt $attempt/$MAX_RETRY_ATTEMPTS), " +
                        "retrying in ${backoffMs}ms: ${e.message}")
                    delay(backoffMs)
                } else {
                    Log.e(TAG, "Flush failed permanently after $MAX_RETRY_ATTEMPTS attempts, " +
                        "dropping ${eventsToFlush.size} events: ${e.message}")
                }
            }
        }
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    /**
     * Get the current authenticated user's profile ID, or null if not logged in.
     */
    private fun getCurrentProfileId(): String? {
        return try {
            supabaseClient.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the current timestamp in ISO 8601 format.
     */
    private fun nowIso8601(): String {
        return Instant.now()
            .atOffset(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_INSTANT)
    }

    /**
     * Get the current number of queued events (for testing/debugging).
     */
    internal suspend fun queueSize(): Int {
        mutex.withLock {
            return eventQueue.size
        }
    }
}

// ============================================================================
// Data Models
// ============================================================================

/**
 * Represents a single engagement event to be written to Supabase.
 *
 * Maps to the `engagement_events` table in the database.
 */
@Serializable
data class EngagementEvent(
    @SerialName("event_type") val eventType: String,
    @SerialName("content_type") val contentType: String,
    @SerialName("content_id") val contentId: String,
    @SerialName("profile_id") val profileId: String? = null,
    @SerialName("metadata") val metadata: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String
)
