package com.foodshare.features.arrangement.data.repository

import com.foodshare.core.realtime.RealtimeChannelManager
import com.foodshare.core.realtime.RealtimeFilter
import com.foodshare.features.arrangement.domain.model.Arrangement
import com.foodshare.features.arrangement.domain.model.ArrangementStatus
import com.foodshare.features.arrangement.domain.repository.ArrangementRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase implementation of ArrangementRepository.
 *
 * Manages arrangement creation, status updates, and real-time subscriptions.
 */
@Singleton
class SupabaseArrangementRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val realtimeManager: RealtimeChannelManager
) : ArrangementRepository {

    private val currentUserId: String?
        get() = supabaseClient.auth.currentUserOrNull()?.id

    override fun observeArrangements(userId: String): Flow<List<Arrangement>> {
        val filter = RealtimeFilter(
            table = "arrangements",
            filter = "or(requester_id.eq.$userId,owner_id.eq.$userId)"
        )

        return channelFlow {
            // First, fetch initial data
            val initial = runCatching {
                supabaseClient.from("arrangements")
                    .select {
                        filter {
                            or {
                                eq("requester_id", userId)
                                eq("owner_id", userId)
                            }
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Arrangement>()
            }.getOrElse { emptyList() }

            send(initial)

            // Then subscribe to updates
            realtimeManager.subscribe<Arrangement>(filter)
                .map { change ->
                    // Refetch all arrangements on any change
                    runCatching {
                        supabaseClient.from("arrangements")
                            .select {
                                filter {
                                    or {
                                        eq("requester_id", userId)
                                        eq("owner_id", userId)
                                    }
                                }
                                order("created_at", Order.DESCENDING)
                            }
                            .decodeList<Arrangement>()
                    }.getOrElse { emptyList() }
                }
                .collect { arrangements ->
                    send(arrangements)
                }
        }
    }

    override suspend fun getArrangement(id: String): Result<Arrangement> {
        return runCatching {
            supabaseClient.from("arrangements")
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingle<Arrangement>()
        }
    }

    override suspend fun createArrangement(
        listingId: Int,
        ownerId: String,
        pickupDate: String?,
        pickupTime: String?,
        pickupLocation: String?,
        notes: String?
    ): Result<Arrangement> {
        val userId = currentUserId ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )

        return runCatching {
            supabaseClient.from("arrangements")
                .insert(
                    mapOf(
                        "listing_id" to listingId,
                        "requester_id" to userId,
                        "owner_id" to ownerId,
                        "status" to ArrangementStatus.PENDING.name.lowercase(),
                        "pickup_date" to pickupDate,
                        "pickup_time" to pickupTime,
                        "pickup_location" to pickupLocation,
                        "notes" to notes
                    )
                ) {
                    select()
                }
                .decodeSingle<Arrangement>()
        }
    }

    override suspend fun updateStatus(id: String, status: ArrangementStatus): Result<Arrangement> {
        return runCatching {
            supabaseClient.from("arrangements")
                .update(
                    mapOf("status" to status.name.lowercase())
                ) {
                    filter { eq("id", id) }
                    select()
                }
                .decodeSingle<Arrangement>()
        }
    }

    override suspend fun getArrangementsForListing(listingId: Int): Result<List<Arrangement>> {
        return runCatching {
            supabaseClient.from("arrangements")
                .select {
                    filter { eq("listing_id", listingId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Arrangement>()
        }
    }
}
