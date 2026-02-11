package com.foodshare.features.help.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.help.presentation.components.FAQItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpCenterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HelpCenterUiState())
    val uiState: StateFlow<HelpCenterUiState> = _uiState.asStateFlow()

    init {
        loadFAQs()
    }

    private fun loadFAQs() {
        viewModelScope.launch {
            val faqSections = listOf(
                FAQSection(
                    title = "Getting Started",
                    items = listOf(
                        FAQItem(
                            question = "How do I create my first listing?",
                            answer = "Tap the '+' button on the main screen, take a photo of your food, add details like quantity and pickup times, and publish. It's that simple!"
                        ),
                        FAQItem(
                            question = "How do I find food near me?",
                            answer = "Open the Map tab to see all available food in your area. Use the search bar to filter by food type or location. You can adjust your search radius in Settings."
                        ),
                        FAQItem(
                            question = "Do I need to verify my account?",
                            answer = "Email verification is required to share food. This helps us maintain trust and safety in the community. You can browse as a guest without verification."
                        )
                    )
                ),
                FAQSection(
                    title = "Sharing Food",
                    items = listOf(
                        FAQItem(
                            question = "What foods can I share?",
                            answer = "You can share surplus food, homemade meals, produce from your garden, or packaged items. Please ensure all food is safe, fresh, and properly stored."
                        ),
                        FAQItem(
                            question = "How do I arrange pickup?",
                            answer = "When someone requests your food, you'll receive a notification. Chat with them to arrange a convenient pickup time and location. Always communicate through the app for safety."
                        ),
                        FAQItem(
                            question = "Can I share cooked food?",
                            answer = "Yes! Many users share homemade meals. Please include preparation details and allergen information. Follow local food safety guidelines for cooked items."
                        )
                    )
                ),
                FAQSection(
                    title = "Safety",
                    items = listOf(
                        FAQItem(
                            question = "How does FoodShare ensure safety?",
                            answer = "We use email verification, user reviews, and AI moderation. All users must agree to our Community Guidelines. Report any suspicious activity immediately."
                        ),
                        FAQItem(
                            question = "What if I receive unsafe food?",
                            answer = "Do not consume it. Report the listing immediately using the Report button. Our team will investigate and take appropriate action."
                        ),
                        FAQItem(
                            question = "Are there pickup safety guidelines?",
                            answer = "Meet in public places when possible, bring a friend, and let someone know where you're going. Always communicate through the app before meeting."
                        )
                    )
                ),
                FAQSection(
                    title = "Account",
                    items = listOf(
                        FAQItem(
                            question = "How do I edit my profile?",
                            answer = "Go to Profile > Edit Profile to update your name, bio, profile picture, and preferences. Your changes are saved automatically."
                        ),
                        FAQItem(
                            question = "Can I delete my account?",
                            answer = "Yes. Go to Settings > Privacy > Delete Account. This action is permanent and cannot be undone. All your data will be removed within 30 days."
                        ),
                        FAQItem(
                            question = "How do I enable two-factor authentication?",
                            answer = "Go to Settings > Security > Two-Factor Authentication. Scan the QR code with your authenticator app and enter the verification code."
                        )
                    )
                ),
                FAQSection(
                    title = "Technical",
                    items = listOf(
                        FAQItem(
                            question = "Why isn't my location working?",
                            answer = "Check that you've granted location permissions in your phone settings. Go to Settings > Apps > FoodShare > Permissions and enable Location."
                        ),
                        FAQItem(
                            question = "I'm not receiving notifications",
                            answer = "Ensure notifications are enabled in Settings > Notifications. Also check your phone's system settings for FoodShare notification permissions."
                        ),
                        FAQItem(
                            question = "How do I report a bug?",
                            answer = "Use the Send Feedback option in Settings. Select 'Bug' as the category and describe the issue. Screenshots are helpful!"
                        )
                    )
                )
            )

            _uiState.update {
                it.copy(
                    faqSections = faqSections,
                    filteredSections = faqSections,
                    isLoading = false
                )
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            _uiState.update { it.copy(filteredSections = it.faqSections) }
            return
        }

        val filtered = _uiState.value.faqSections.mapNotNull { section ->
            val matchingItems = section.items.filter { item ->
                item.question.contains(query, ignoreCase = true) ||
                item.answer.contains(query, ignoreCase = true)
            }
            if (matchingItems.isNotEmpty()) {
                section.copy(items = matchingItems)
            } else {
                null
            }
        }

        _uiState.update { it.copy(filteredSections = filtered) }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                filteredSections = it.faqSections
            )
        }
    }
}

data class HelpCenterUiState(
    val faqSections: List<FAQSection> = emptyList(),
    val filteredSections: List<FAQSection> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

data class FAQSection(
    val title: String,
    val items: List<FAQItem>
)
