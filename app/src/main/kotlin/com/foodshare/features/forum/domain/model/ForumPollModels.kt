package com.foodshare.features.forum.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant

/**
 * Forum poll models for voting functionality.
 * Maps to forum_polls, forum_poll_options, forum_poll_votes tables.
 *
 * SYNC: This mirrors Swift FoodshareCore.ForumPoll (ForumPoll.swift)
 */

// MARK: - Poll Type

/**
 * Poll voting type - single choice or multiple choice.
 *
 * SYNC: This mirrors Swift PollType
 */
@Serializable
enum class PollType {
    @SerialName("single") SINGLE,
    @SerialName("multiple") MULTIPLE;

    val displayName: String
        get() = when (this) {
            SINGLE -> "Single Choice"
            MULTIPLE -> "Multiple Choice"
        }

    val iconName: String
        get() = when (this) {
            SINGLE -> "radio_button_checked"
            MULTIPLE -> "check_box"
        }
}

// MARK: - Forum Poll

/**
 * Represents a poll attached to a forum post.
 *
 * SYNC: This mirrors Swift FoodshareCore.ForumPoll
 */
@Serializable
data class ForumPoll(
    val id: String,
    @SerialName("forum_id") val forumId: Int,
    val question: String,
    @SerialName("poll_type") val pollType: PollType,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("is_anonymous") val isAnonymous: Boolean = false,
    @SerialName("show_results_before_vote") val showResultsBeforeVote: Boolean = false,
    @SerialName("total_votes") val totalVotes: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Joined data
    val options: List<ForumPollOption>? = null,
    @SerialName("user_votes") val userVotes: List<String>? = null
) {
    // MARK: - Computed Properties

    /** Whether the poll has ended. */
    val hasEnded: Boolean
        get() {
            val endTime = endsAt ?: return false
            return try {
                Instant.parse(endTime).isBefore(Instant.now())
            } catch (_: Exception) {
                false
            }
        }

    /** Whether the poll is currently active. */
    val isActive: Boolean
        get() = !hasEnded

    /** Whether the current user has already voted. */
    val hasVoted: Boolean
        get() = !userVotes.isNullOrEmpty()

    /** Whether to show results (either voted, ended, or allowed before voting). */
    val shouldShowResults: Boolean
        get() = hasVoted || hasEnded || showResultsBeforeVote

    /** Whether the user can vote (active poll, respecting poll type constraints). */
    val canVote: Boolean
        get() {
            if (hasEnded) return false
            if (pollType == PollType.SINGLE) return !hasVoted
            return true // Multiple choice allows changing votes
        }

    /** Time remaining until poll ends, or null if no end date or already ended. */
    val timeRemainingSeconds: Long?
        get() {
            val endTime = endsAt ?: return null
            if (hasEnded) return null
            return try {
                val end = Instant.parse(endTime)
                Duration.between(Instant.now(), end).seconds.coerceAtLeast(0)
            } catch (_: Exception) {
                null
            }
        }

    /** Formatted time remaining string. */
    val timeRemainingText: String?
        get() {
            val remaining = timeRemainingSeconds ?: return null
            val hours = (remaining / 3600).toInt()
            val days = hours / 24

            return when {
                days > 0 -> "$days day${if (days == 1) "" else "s"} left"
                hours > 0 -> "$hours hour${if (hours == 1) "" else "s"} left"
                else -> {
                    val minutes = maxOf(1, (remaining / 60).toInt())
                    "$minutes minute${if (minutes == 1) "" else "s"} left"
                }
            }
        }
}

// MARK: - Forum Poll Option

/**
 * Represents an option in a forum poll.
 *
 * SYNC: This mirrors Swift FoodshareCore.ForumPollOption
 */
@Serializable
data class ForumPollOption(
    val id: String,
    @SerialName("poll_id") val pollId: String,
    @SerialName("option_text") val optionText: String,
    @SerialName("votes_count") val votesCount: Int = 0,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
) {
    /** Calculate percentage of votes for this option. */
    fun votePercentage(totalVotes: Int): Double =
        if (totalVotes > 0) votesCount.toDouble() / totalVotes * 100 else 0.0

    /** Formatted percentage string. */
    fun formattedPercentage(totalVotes: Int): String {
        val percentage = votePercentage(totalVotes)
        return "${percentage.toInt()}%"
    }
}

// MARK: - Forum Poll Vote

/**
 * Represents a user's vote on a poll option.
 *
 * SYNC: This mirrors Swift FoodshareCore.ForumPollVote
 */
@Serializable
data class ForumPollVote(
    val id: String,
    @SerialName("poll_id") val pollId: String,
    @SerialName("option_id") val optionId: String,
    @SerialName("profile_id") val profileId: String,
    @SerialName("created_at") val createdAt: String? = null
)

// MARK: - Create Poll Request

/**
 * Request model for creating a new poll.
 *
 * SYNC: This mirrors Swift CreatePollRequest
 */
@Serializable
data class CreatePollRequest(
    @SerialName("forum_id") val forumId: Int,
    val question: String,
    @SerialName("poll_type") val pollType: PollType = PollType.SINGLE,
    val options: List<String>,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("is_anonymous") val isAnonymous: Boolean = false,
    @SerialName("show_results_before_vote") val showResultsBeforeVote: Boolean = false
)

// MARK: - Vote Poll Request

/**
 * Request model for voting on a poll.
 *
 * SYNC: This mirrors Swift VotePollRequest
 */
@Serializable
data class VotePollRequest(
    @SerialName("poll_id") val pollId: String,
    @SerialName("option_ids") val optionIds: List<String>,
    @SerialName("profile_id") val profileId: String
)

// MARK: - Poll Results

/**
 * Aggregated poll results.
 *
 * SYNC: This mirrors Swift ForumPollResults
 */
@Serializable
data class ForumPollResults(
    val poll: ForumPoll,
    val options: List<ForumPollOption>,
    @SerialName("total_votes") val totalVotes: Int,
    @SerialName("voter_count") val voterCount: Int,
    @SerialName("user_voted_option_ids") val userVotedOptionIds: List<String> = emptyList()
) {
    /** The winning option (most votes). */
    val winningOption: ForumPollOption?
        get() = options.maxByOrNull { it.votesCount }

    /** Whether there's a clear winner (more than 50%). */
    val hasClearWinner: Boolean
        get() {
            val winner = winningOption ?: return false
            if (totalVotes <= 0) return false
            return winner.votesCount.toDouble() / totalVotes > 0.5
        }
}
