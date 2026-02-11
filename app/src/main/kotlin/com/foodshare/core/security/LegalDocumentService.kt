package com.foodshare.core.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Legal document types
 */
enum class LegalDocType(val fileName: String, val displayName: String) {
    TERMS_OF_SERVICE("terms_of_service.md", "Terms of Service"),
    PRIVACY_POLICY("privacy_policy.md", "Privacy Policy"),
    OPEN_SOURCE_LICENSES("open_source_licenses.md", "Open Source Licenses")
}

/**
 * Service for fetching and caching legal documents
 */
@Singleton
class LegalDocumentService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) {

    private val documentCache = mutableMapOf<LegalDocType, String>()

    /**
     * Get legal document content
     *
     * First tries to load from Supabase storage, falls back to bundled assets
     *
     * @param type Legal document type
     * @return Document content as string
     */
    suspend fun getDocument(type: LegalDocType): String {
        // Return cached version if available
        documentCache[type]?.let { return it }

        // Try to load from Supabase storage first
        val content = try {
            loadFromSupabase(type)
        } catch (e: Exception) {
            // Fallback to bundled assets
            loadFromAssets(type)
        }

        // Cache the content
        documentCache[type] = content
        return content
    }

    /**
     * Load document from Supabase storage
     */
    private suspend fun loadFromSupabase(type: LegalDocType): String {
        return withContext(Dispatchers.IO) {
            val bucket = supabaseClient.storage.from("legal-documents")
            val bytes = bucket.downloadPublic(type.fileName)
            String(bytes)
        }
    }

    /**
     * Load document from bundled assets
     */
    private suspend fun loadFromAssets(type: LegalDocType): String {
        return withContext(Dispatchers.IO) {
            val assetPath = when (type) {
                LegalDocType.TERMS_OF_SERVICE -> "legal/terms_of_service.md"
                LegalDocType.PRIVACY_POLICY -> "legal/privacy_policy.md"
                LegalDocType.OPEN_SOURCE_LICENSES -> "legal/open_source_licenses.md"
            }

            try {
                context.assets.open(assetPath).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                // Return placeholder content if file not found
                getPlaceholderContent(type)
            }
        }
    }

    /**
     * Get placeholder content for legal documents
     */
    private fun getPlaceholderContent(type: LegalDocType): String {
        return when (type) {
            LegalDocType.TERMS_OF_SERVICE -> """
                # Terms of Service

                **Last Updated: January 2026**

                ## 1. Acceptance of Terms

                By accessing and using FoodShare, you accept and agree to be bound by the terms and provision of this agreement.

                ## 2. Use License

                Permission is granted to temporarily use FoodShare for personal, non-commercial purposes.

                ## 3. User Conduct

                Users agree to:
                - Provide accurate information
                - Respect other users
                - Follow food safety guidelines
                - Not use the service for illegal purposes

                ## 4. Content

                Users are responsible for the content they post. FoodShare reserves the right to remove any content that violates these terms.

                ## 5. Privacy

                Your use of FoodShare is also governed by our Privacy Policy.

                ## 6. Changes to Terms

                FoodShare reserves the right to modify these terms at any time.

                For questions, contact: legal@foodshare.club
            """.trimIndent()

            LegalDocType.PRIVACY_POLICY -> """
                # Privacy Policy

                **Last Updated: January 2026**

                ## 1. Information We Collect

                We collect information you provide directly, such as:
                - Account information (email, name)
                - Profile information
                - Listings and posts
                - Messages and communications

                ## 2. How We Use Your Information

                We use your information to:
                - Provide and improve our services
                - Communicate with you
                - Ensure safety and security
                - Comply with legal obligations

                ## 3. Information Sharing

                We do not sell your personal information. We may share information:
                - With other users (as part of the service)
                - With service providers
                - When required by law

                ## 4. Data Security

                We implement appropriate security measures to protect your data.

                ## 5. Your Rights

                You have the right to:
                - Access your data
                - Correct your data
                - Delete your data
                - Export your data

                ## 6. Contact Us

                For privacy questions, contact: privacy@foodshare.club
            """.trimIndent()

            LegalDocType.OPEN_SOURCE_LICENSES -> """
                # Open Source Licenses

                FoodShare uses the following open source software:

                ## Android Libraries

                ### Jetpack Compose
                - License: Apache 2.0
                - Copyright: Google Inc.

                ### Kotlin
                - License: Apache 2.0
                - Copyright: JetBrains

                ### Supabase-kt
                - License: Apache 2.0
                - Copyright: Supabase

                ### Hilt
                - License: Apache 2.0
                - Copyright: Google Inc.

                ### Coil
                - License: Apache 2.0
                - Copyright: Coil Contributors

                ## Swift Libraries

                ### Swift
                - License: Apache 2.0
                - Copyright: Apple Inc.

                ---

                Full license texts available at: https://github.com/foodshare/licenses
            """.trimIndent()
        }
    }

    /**
     * Clear document cache
     */
    fun clearCache() {
        documentCache.clear()
    }
}
