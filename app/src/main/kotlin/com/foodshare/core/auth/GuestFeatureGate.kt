package com.foodshare.core.auth

import javax.inject.Inject
import javax.inject.Singleton

enum class GuestFeature {
    VIEW_LISTINGS, VIEW_MAP, VIEW_FORUM, VIEW_CHALLENGES,
    CREATE_LISTING, SEND_MESSAGE, LEAVE_REVIEW, JOIN_CHALLENGE,
    EDIT_PROFILE, ACCESS_SETTINGS, VIEW_INSIGHTS, REPORT_CONTENT
}

@Singleton
class GuestFeatureGate @Inject constructor(
    private val guestManager: GuestManager
) {
    private val guestAllowed = setOf(
        GuestFeature.VIEW_LISTINGS, GuestFeature.VIEW_MAP,
        GuestFeature.VIEW_FORUM, GuestFeature.VIEW_CHALLENGES
    )

    fun isAllowed(feature: GuestFeature): Boolean {
        if (!guestManager.isGuestMode()) return true
        return feature in guestAllowed
    }

    fun requiresAuth(feature: GuestFeature): Boolean = feature !in guestAllowed
}
