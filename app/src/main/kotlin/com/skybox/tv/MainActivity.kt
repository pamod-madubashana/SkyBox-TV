package com.skybox.tv

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.ui.auth.AuthScreen
import com.skybox.tv.ui.auth.AuthorizedScreen
import com.skybox.tv.ui.chat.ChatListScreen
import com.skybox.tv.ui.player.PlayerScreen
import com.skybox.tv.ui.theme.SkyBoxTvTheme
import com.skybox.tv.domain.model.MediaItem

/**
 * MainActivity for SkyBox TV.
 * Displays the Telegram authentication screen as the entry point.
 * After authorization, the user can browse chats and play media.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkyBoxTvTheme {
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                // Track authentication state for navigation
                val authState by remember { mutableStateFlow<AuthState>(AuthState.NotConfigured) }

                // Navigation state
                val showChatList by remember { mutableStateOf(false) }
                val selectedMediaItemBy by remember { MutableStateFlow<MediaItem?>(null) }

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
                                    authState.value = AuthState.Authorized
                                },
                                onListChats = {
                                    // User clicked "List My Chats" - show chat list
                                    showChatList.value = true
                                },
                                onLogout = {
                                    authState.value = AuthState.LoggingOut
                                }
                            )
                        }

                        is AuthState.Authorized -> {
                            // Show authorized screen with chat access
                            // If chat list is shown, display that; otherwise show authorized UI
                            if (showChatList.value) {
                                // Show chat list screen with simple navigation
                                ChatListScreen(
                                    onChatSelected = { chat ->
                                        // For proof of concept: create a media item from the selected chat
                                        // and navigate to player screen
                                        val placeholderMedia = MediaItem(
                                            mediaId = chat.id.toLong() * 1000L + 1L,
                                            chatId = chat.id,
                                            messageId = 1L,
                                            fileName = "Video from chat",
                                            mimeType = "video/mp4",
                                            sizeBytes = 1024L,
                                            durationMs = 300000L
                                        )
                                        selectedMediaItemBy.value = placeholderMedia
                                        showChatList.value = false
                                    },
                                    focusRequester = focusRequester
                                )
                            } else {
                                // Show authorized screen UI
                                AuthorizedScreen(
                                    accountInfo = "Account ID: ${authState.value == AuthState.Authorized ? 123456 : 0}",
                                    onListChats = {
                                        // User clicked "List My Chats" - show chat list
                                        showChatList.value = true
                                    },
                                    onLogout = {
                                        authState.value = AuthState.LoggingOut
                                    }
                                )
                            }
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
                                    authState.value = AuthState.NotConfigured
                                }
                            )
                        }
                    }
                }

                // Player screen - shown when a media item is selected
                val playerState by remember { mutableStateFlow<Boolean>(false) }
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
                                authState.value = AuthState.NotConfigured
                            }
                        )
                    }
                    else -> {
                        // Default: show based on auth state (handled by remember above)
                        Unit
                    }
                }
            }
        }
    }
}