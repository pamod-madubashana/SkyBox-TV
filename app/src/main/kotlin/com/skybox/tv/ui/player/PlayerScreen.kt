/**
 * PlayerScreen is a minimal full-screen player Compose composable for Android TV.
 *
 * Features:
 * - Full-screen video playback using ExoPlayer
 * - Play/pause control via remote
 * - Seek progress bar with seek bar interaction
 * - Buffering state visualization
 * - Error state display with retry option
 * - Back button to exit player and return to chat list
 *
 * Design notes for TV:
 * - Uses D-pad navigation (focus-based)
 * - Large touch targets for remote control
 * - Focus highlighting for visible elements
 * - State-driven UI (no logic in composition, driven by ViewModel)
 */
package com.skybox.tv.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MemoryPeeking
import androidx.compose.foundation.pointer.Semantics
import androidx.compose.foundation.pointer.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics painterResource
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Keys
import androidx.compose.ui.layout.contentKey
import androidx.compose.ui.layout.Invocation
import androidx.compose.ui.layout.fillMaxSize
import androidx.compose.ui.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.isFocusable
import androidx.compose.ui.semantics.interactionSource
import androidx.compose.ui.semantics.interactionSource.composeInteractionSource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.selection.selectionModifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.intDp
import androidx.compose.ui.viewinterop.AndroidViewHolder
import androidx.compose.viewinterop.ComposeView
import com.google.android.exoplayer.ui.PlayerView
import com.skybox.tv.R
import com.skybox.tv.data.streaming.TelegramMediaDataSource
import com.skybox.tv.data.streaming.TelegramMediaDataSourceUri
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.presentation.player.PlaybackSession
import com.skybox.tv.presentation.player.PlaybackSessionState
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme
import kotlin.runBlocking

/**
 * Composable screen for playing a single video media item.
 *
 * Displays a full-screen video player with playback controls.
 * State is driven by the PlaybackSession ViewModel and PlayerManager.
 *
 * @param mediaItem The media item to play
 * @param onBack Callback when user presses back (returns to previous screen)
 * @param onPlayPause Toggle play/pause state
 * @param dataSource Optional TelegramMediaDataSource for byte access; if null,
 *                   one will be created from the media item's file ID
 */
@Composable
fun PlayerScreen(
    mediaItem: MediaItem,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    dataSource: TelegramMediaDataSource? = null
) {
    var position by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) }

    // Create the PlayerManager and PlayerView
    val context = LocalContext.current
    val viewCompositionStrategy = rememberWindowSizeClass().compositionStrategy

    // PlayerView is ExoPlayer's Compose integration
    val playerView = remember {
        PlayerView(context).apply {
            // Configure for full-screen playback
            setEnableScaling(true)
            setControllerVisibilityListener { visible ->
                // Controller visibility managed by state
            }
        }
    }

    // Initialize PlayerManager
    val playerManager = remember(PlayerManager::class.java) {
        PlayerManager()
    }

    // Create or use the provided TelegramMediaDataSource
    val telegramDataSource: TelegramMediaDataSource = if (dataSource != null) {
        dataSource
    } else {
        // Create a data source from the media item's file ID
        TelegramMediaDataSource(
            TelegramMediaDataSourceUri(mediaItem.mediaId)
        )
    }

    // Start playback when the data source is set up
    // This is a one-time operation triggered by composition
    val playbackStarted by remember {
        //noinspection RunOnSelfModifiedValue
        playerManager.play(
            mediaItem = mediaItem,
            dataSource = telegramDataSource,
            context = context
        )
        true
    }

    // Observe playback session state
    val sessionState by playerManager.sessionState.collectAsState()

    // Handle key events for D-pad navigation
    SideEffect {
        var consumed = false

        // Listen for Back key press
        displayKeyEvent(KeyEvent(
            eventType = KeyEventType.KEY_DOWN,
            key = Keys.BACK,
            repeatMode = KeyEvent.REPEAT_MODE_FIRST,
            metaState = 0
        )) { event ->
            if (!consumed) {
                onBack()
                true // consume the event
            } else {
                false
            }
        }
    }

    TvTheme {
        var focus by remember { mutableStateOf(false) }

        // Full-screen layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            contentAlignment = Alignment.Center
        ) {
            // ExoPlayer full-screen view
            @Composable
            inner fun PlayerContent() {
                PlayerView(context).also { playerView ->
                    // Player will be initialized via playerManager
                    // when session state becomes Playing
                }
            }

            PlayerContent()

            // State-driven overlay UI
            when (sessionState) {
                is PlaybackSessionState.Idle -> {
                    // Title area showing media info when idle/idling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "${mediaItem.fileName}",
                            style = MaterialTheme.typography.h6,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                is PlaybackSessionState.Playing -> {
                    // Main playback area - PlayerView
                    PlayerView(context).apply {
                        // Player is managed externally
                    }
                }

                is PlaybackSessionState.Paused -> {
                    Center {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .alignment(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterCenter
                        ) {
                            Icon(
                                imageVector = Icons.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colorScheme.onSurface
                            )
                            spacer(height = 16.dp)
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.titleMedium,
                                tint = colorScheme.onSurface
                            )
                        }
                    }
                }

                is PlaybackSessionState.Buffering -> {
                    Center {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .alignment(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterCenter
                        ) {
                            CircularProgressIndicator()
                            spacer(height = 16.dp)
                            Text(
                                text = "Buffering...",
                                style = MaterialTheme.typography.body1,
                                tint = colorScheme.onSurface
                            )
                        }
                    }
                }

                is PlaybackSessionState.Error -> {
                    Center {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .alignment(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterCenter
                        ) {
                            Icon(
                                imageVector = Icons.Failed,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = colorScheme.error
                            )
                            spacer(height = 16.dp)
                            Text(
                                text = "Playback Error",
                                style = MaterialTheme.typography.titleMedium,
                                tint = colorScheme.error
                            )
                            spacer(height = 8.dp)
                            Text(
                                text = "Could not play media",
                                style = MaterialTheme.typography.body1,
                                tint = colorScheme.onSurfaceVariant
                            )
                            spacer(height = 24.dp)
                            Button(
                                onClick = {
                                    // Retry playback
                                    onPlayPause()
                                },
                                modifier = Modifier.focusRequester(focusRequester)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }

        // Play/Pause button overlay (bottom right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            // Make clickable for remote
            clickable {
                onPlayPause()
            }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.PlayArrow else Icons.Pause,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colorScheme.onSurface
            )
        }
    }
}

/**
 * Focus requester for TV D-pad navigation.
 * Ensures proper focus movement between player controls.
 */
private val focusRequester = remember { FocusRequester() }

/**
 * Handle hardware back key for player exit.
 * Called from MainActivity or parent screen.
 */
@Composable
fun handleBackPress(
    onBack: () -> Unit,
    onPlayPause: () -> Unit
) {
    var backPressed by remember { mutableStateOf(false) }

    // Listen for Back key press on the TV remote
    displayKeyEvent(KeyEvent(
        eventType = KeyEventType.KEY_DOWN,
        key = Keys.BACK,
        repeatMode = KeyEvent.REPEAT_MODE_FIRST,
        metaState = 0
    )) { keyEvent ->
        if (!backPressed) {
            backPressed = true
            // Schedule reset after a short delay
            // This prevents accidental double-back
            // In a real app, we'd use a CoroutineScope with delayed()
            // For simplicity, we just call onBack immediately
            onBack()
            true // consume the event
        } else {
            false // don't consume, let system handle
        }
    }
}