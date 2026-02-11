package com.foodshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.foodshare.core.deeplink.DeepLinkNavigator
import com.foodshare.core.push.DeepLinkBuilder
import com.foodshare.features.auth.presentation.AuthViewModel
import com.foodshare.features.onboarding.data.OnboardingPreferences
import com.foodshare.ui.navigation.AppNavGraph
import com.foodshare.ui.navigation.NavRoutes
import com.foodshare.ui.theme.FoodShareTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var onboardingPreferences: OnboardingPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FoodShareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var pendingDeepLink by remember { mutableStateOf<Uri?>(intent?.data) }

                    // Handle deep links
                    LaunchedEffect(pendingDeepLink) {
                        pendingDeepLink?.let { uri ->
                            handleDeepLink(uri, navController)
                            pendingDeepLink = null
                        }
                    }

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        onboardingPreferences = onboardingPreferences
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            handleDeepLinkIntent(uri)
        }
    }

    /**
     * Handle deep link from intent (when app is already running)
     */
    private fun handleDeepLinkIntent(uri: Uri) {
        // For OAuth callbacks, handle directly
        if (uri.scheme == "club.foodshare" && uri.host == "auth") {
            authViewModel.handleOAuthCallback(uri.toString())
            return
        }

        // For other deep links, we need to restart the activity to properly navigate
        // This is handled via the composable state
    }

    /**
     * Handle deep link navigation using DeepLinkNavigator.
     *
     * Supports both app scheme (foodshare://) and web URLs (https://foodshare.club/).
     */
    private fun handleDeepLink(
        uri: Uri,
        navController: androidx.navigation.NavController
    ) {
        val scheme = uri.scheme
        val host = uri.host

        // OAuth callback
        if (scheme == "club.foodshare" && host == "auth") {
            authViewModel.handleOAuthCallback(uri.toString())
            return
        }

        // Use DeepLinkNavigator for all deep links (foodshare:// and https://foodshare.club/)
        DeepLinkNavigator.navigate(navController, uri.toString())
    }
}
