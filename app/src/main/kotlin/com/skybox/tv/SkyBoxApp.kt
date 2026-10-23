package com.skybox.tv

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.navigation.NavHostController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.navigation.foundation.NavHostController
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.ui.auth.AuthScreen
import com.skybox.tv.ui.navigation.TopNavigation
import com.skybox.tv.ui.theme.SkyBoxTvTheme
import com.skybox.tv.domain.model.MediaItem

/**
 * Main application composable for SkyBox TV.
 * Handles authentication state and top-level navigation.
 * Shows AuthScreen if not authorized, otherwise shows the TopNavigation
 * with access to all main destinations.
 */
@Composable
fun SkyBoxApp() {
    var authState by remember { mutableStateFlow<AuthState>(AuthState.NotConfigured) }
    val navController = rememberNavController()
    val focusRequester = remember { FocusRequester() }

    // Track navigation state
    val showChatList by remember { mutableStateOf(false) }
    val selectedMediaItemBy by remember { MutableStateFlow<MediaItem?>(null) }
    val playerState by remember { mutableStateFlow<Boolean>(false) }

    // When auth state changes, update the UI
    remember(authState) { currentState ->
        when (currentState) {
            is AuthState.NotConfigured, is AuthState.Initializing,
            is AuthState.WaitingForPhoneNumber, is AuthState.WaitingForCode,
            is AuthState.WaitingForPassword -> {
                // Show authentication screen
                AuthScreen(
                    onAuthorized = { account ->
                        // Transition to authorized state
                        authState = AuthState.Authorized
                    },
                    onListChats = {
                        // User clicked "List My Chats" - show chat list
                        showChatList.value = true
                    },
                    onLogout = {
                        authState = AuthState.LoggingOut
                    }
                )
            }

            is AuthState.Authorized -> {
                // Show top navigation with all destinations
                TopNavigation(
                    authenticated = true,
                    onSignOut = {
                        authState = AuthState.LoggingOut
                    },
                    destinationState = navController,
                    onDestinationChanged = { destination ->
                        // Handle destination changes
                        // Could update UI based on selected destination
                    },
                    focusRequester = focusRequester
                )
            }

            is AuthState.LoggingOut -> {
                Center {
                    Text("Logging out from Telegram...")
                    spacer(height = 16.dp)
                    CircularProgressIndicator()
                }
            }

            is AuthState.Error -> {
                ErrorState(
                    message = (currentState as AuthState.Error).message,
                    onRetry = {},
                    onBack = {
                        authState = AuthState.NotConfigured
                    }
                )
            }
        }
    }

    // Player screen - shown when a media item is selected
    LaunchedEffect(selectedMediaItemBy) {
        if (selectedMediaItemBy.value != null && !playerState.value) {
            playerState.value = true
        }
    }

    when {
        is playerState.value -> {
            // Show player screen with the selected media item
            PlayerScreen(
                mediaItem = selectedMediaItemBy.value!!,
                onBack = { playerState.value = false; showChatList.value = false },
                onPlayPause = { /* toggle play/pause */ }
            )
        }
        is authState.value is AuthState.Error -> {
            ErrorState(
                message = (authState.value as AuthState.Error).message,
                onRetry = {},
                onBack = {
                    authState = AuthState.NotConfigured
                }
            )
        }
        else -> {
            // Default: show based on auth state (handled by remember above)
            Unit
        }
    }
}

/** Error state UI for the main app. */
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(16.dp)
        )

        spacer(height = 16.dp)

        Button(onClick = onRetry, modifier = Modifier.padding(4.dp)) {
            Text("Retry")
        }

        spacer(height = 8.dp)

        Button(onClick = onBack, modifier = Modifier.padding(4.dp)) {
            Text("Back")
        }
    }
}