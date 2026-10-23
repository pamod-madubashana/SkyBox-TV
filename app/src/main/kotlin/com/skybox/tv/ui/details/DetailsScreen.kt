package com.skybox.tv.ui.details

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pointer.keyboardKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Keys
import androidx.compose.ui.input.key.displayKeyEvent
import androidx.compose.ui.unit.dp
import com.skybox.tv.data.catalog.MediaCatalogRepository
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.domain.usecase.SearchMediaUseCase
import com.skybox.tv.presentation.player.PlaybackSessionState
import com.skybox.tv.presentation.player.PlayerLifecycleManager
import com.skybox.tv.ui.components.ActionButtons
import com.skybox.tv.ui.components.DetailInfo
import com.skybox.tv.ui.components.SearchBar
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme
import com.skybox.tv.R
import kotlin.coroutines.resumeWithException

/**
 * Details screen showing media item information and playback controls.
 * Designed for Android TV D-pad navigation.
 *
 * Shows: filename/title, duration, size, media type, source chat, thumbnail
 * Actions: Play, Resume, Restart
 * Focus management for D-pad
 * Back navigation
 */
@Composable
fun DetailsScreen(
    mediaItem: MediaItem,
    onBack: () -> Unit,
    catalogRepository: MediaCatalogRepository,
    onPlay: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit
) {
    val state = remember { mutableStateOf<PlaybackSessionState?>(null) }
    val focusRequester = remember { FocusRequester() }

    // Collect playback state changes
    val lifecycleManager = remember(PlayerLifecycleManager()) {
        PlayerLifecycleManager()
    }

    lifecycleManager.sessionState.collect { sessionState ->
        state.value = sessionState
    }

    SkyBoxTvTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Center
        ) {
            // Header with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Back button
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .focusRequester(focusRequester)
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Title area
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = mediaItem.fileName,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .maxLines(2)
                        .overflow = TextOverflow.Ellipsis,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
            }

            // Divider
            Divider(modifier = Modifier.height(1.dp), color = colorScheme.onSurfaceVariant)

            // Media details
            DetailInfo(
                mediaItem = mediaItem,
                showThumbnail = true
            )

            spacer(height = 16.dp)

            // Playback action buttons
            ActionButtons(
                mediaItem = mediaItem,
                onPlay = {
                    // Play from beginning
                    onPlay()
                },
                onResume = {
                    // Resume from saved position
                    onResume()
                },
                onRestart = {
                    // Restart from beginning
                    onRestart()
                },
                currentState = state.value,
                dataSource = null
            )

            spacer(height = 24.dp)

            // Focus indicator for D-pad
            Center {
                Text(
                    text = "Use remote to navigate",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Preview composable for DetailsScreen showing a grid of media items.
 * Each item navigates to the DetailsScreen when selected.
 */
@Composable
fun DetailsScreenPreview(
    mediaItems: List<MediaItem>,
    onItemSelected: (MediaItem) -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    SkyBoxTvTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Center
        ) {
            // Header
            Text(
                text = "Media Details",
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Grid of media items
            if (mediaItems.isEmpty()) {
                emptyState(focusRequester)
            } else {
                GridRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    span = 2,
                    vAlignment = Alignment.CenterVertically
                ) {
                    mediaItems.forEach { mediaItem ->
                        DetailsItemCard(
                            mediaItem = mediaItem,
                            onSelect = { onItemSelected(mediaItem) },
                            focusRequester = focusRequester
                        )
                    }
                }
            }

            spacer(height = 16.dp)

            if (mediaItems.isNotEmpty()) {
                DetailsActionButtons(
                    mediaItem = mediaItems.first(),
                    onBack = onBack
                )
            }
        }
    }
}

/** Individual media item card in the details grid. */
@Composable
private fun DetailsItemCard(
    mediaItem: MediaItem,
    onSelect: () -> Unit,
    focusRequester: androidx.compose.ui.focus.FocusRequester
) {
    Card(
        modifier = Modifier
            .size(140.dp)
            .focusRequester(focusRequester)
            .clickable(onClick = onSelect),
        elevation = androidx.cardwidget.CardElevation.Normal
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterStart
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Thm",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.Center
                )
            }

            spacer(height = 4.dp)

            // Title (truncated)
            Text(
                text = mediaItem.fileName,
                style = MaterialTheme.typography.caption,
                modifier = Modifier
                    .fillMaxWidth()
                    .maxLines(1)
                    .overflow = TextOverflow.Ellipsis,
                horizontalAlignment = Alignment.Center
            )
        }
    }
}

/** Empty state when no media items are available. */
private fun emptyState(focusRequester: androidx.compose.ui.focus.FocusRequester) {
    Center {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outline.File,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colorScheme.onSurfaceVariant
            )
            spacer(height = 8.dp)
            Text(
                text = "No Media",
                style = MaterialTheme.typography.titleMedium
            )
            spacer(height = 16.dp)
            Text(
                text = "No videos found. Search to find content.",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(300.dp)
            )
        }
    }
}

/** Action buttons specifically for the details screen. */
@Composable
private fun DetailsActionButtons(
    mediaItem: MediaItem,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalArrangement = Arrangement.SpaceEvenly,
        navigationMode = NavigationMode.Horizontal
    ) {
        OutlinedButton(
            onClick = { onBack() },
            modifier = Modifier
                .size(70.dp)
                .focusRequester(remember { FocusRequester() })
        ) {
            Text("Back")
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .size(70.dp)
                .focusRequester(remember { FocusRequester() })
        ) {
            Text("Play")
        }
    }
}