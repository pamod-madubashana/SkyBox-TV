/**
 * HomeScreen — Compose TV home screen with featured content and rows.
 * Shows a Netflix-style home screen with Continue Watching, Recently Added, and Recently Watched rows.
 * Uses MediaCatalogRepository for data source.
 * Designed for D-pad navigation on Android TV.
 */
package com.skybox.tv.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skybox.tv.data.catalog.MediaCatalogRepository
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.components.ContentRow
import com.skybox.tv.ui.components.ContentRowWithHeader
import com.skybox.tv.ui.components.ErrorState
import com.skybox.tv.ui.components.LoadingState
import com.skybox.tv.ui.components.MediaCard
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow collectLatest
import kotlinx.coroutines.flow flow {
    it.collect()
}
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * When to show which state:
 * - Loading: Repository is fetching data
 * - Empty: No data available
 * - Error: Repository encountered an error
 * - Content: Data is available and displayed
 */
@Composable
fun HomeScreen(
    repository: MediaCatalogRepository = roomMediaCatalogRepository(),
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    var selectedItem by remember { mutableStateOf<MediaItem?>(null) }

    // State flows for each row section
    val continueWatchingFlow = repository.getByChatId("-1") // Special placeholder
    val recentlyAddedFlow = repository.getAllPaged()
    val recentlyWatchedFlow = repository.getByChatId("-2") // Special placeholder

    // Combined state
    val homeState by remember {
        mutableStateOf(HomeUiState.Loading)
    }

    // Collect flows from repository
    var continueWatchingItems by remember { mutableStateListOf<MediaItem>() }
    var recentlyAddedItems by remember { mutableStateListOf<MediaItem>() }
    var recentlyWatchedItems by remember { mutableStateListOf<MediaItem>() }

    // Collect data from repository flows
    LaunchedEffect(Unit) {
        // Continue Watching - use getByChatId with special chatId
        continueWatchingFlow.collect { items ->
            continueWatchingItems = items
            homeState = when {
                items.isEmpty() && homeState !in listOf(HomeUiState.Error) -> HomeUiState.Empty
                items.isNotEmpty() -> HomeUiState.Content
                else -> homeState
            }
        }
    }

    LaunchedEffect(Unit) {
        recentlyAddedFlow.collect { items ->
            recentlyAddedItems = items
            homeState = when {
                items.isEmpty() && homeState !in listOf(HomeUiState.Error) -> HomeUiState.Empty
                items.isNotEmpty() -> HomeUiState.Content
                else -> homeState
            }
        }
    }

    LaunchedEffect(Unit) {
        recentlyWatchedFlow.collect { items ->
            recentlyWatchedItems = items
            homeState = when {
                items.isEmpty() && homeState !in listOf(HomeUiState.Error) -> HomeUiState.Empty
                items.isNotEmpty() -> HomeUiState.Content
                else -> homeState
            }
        }
    }

    SkyBoxTvTheme {
        when (homeState) {
            is HomeUiState.Loading -> {
                LoadingState(
                    message = "Loading home screen...",
                    focusRequester = focusRequester
                )
            }

            is HomeUiState.Empty -> {
                EmptyState(
                    title = "No Content",
                    description = "No videos found.\nMake sure you have authorized Telegram and selected some chats.",
                    focusRequester = focusRequester
                )
            }

            is HomeUiState.Error -> {
                ErrorState(
                    message = "Failed to load home screen",
                    onRetry = { /* reload data */ },
                    focusRequester = focusRequester
                )
            }

            is HomeUiState.Content -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    // Featured area - single featured item
                    if (continueWatchingItems.isNotEmpty()) {
                        FeaturedItem(
                            mediaItem = continueWatchingItems.first(),
                            onSelect = { selectedItem = it },
                            focusRequester = focusRequester
                        )
                    }

                    spacer(height = 24.dp)

                    // Continue Watching row
                    ContentRowWithHeader(
                        sectionTitle = "Continue Watching",
                        items = continueWatchingItems,
                        onSelect = { selectedItem = it }
                    )

                    spacer(height = 24.dp)

                    // Recently Added row
                    ContentRowWithHeader(
                        sectionTitle = "Recently Added",
                        items = recentlyAddedItems,
                        onSelect = { selectedItem = it }
                    )

                    spacer(height = 24.dp)

                    // Recently Watched row
                    ContentRowWithHeader(
                        sectionTitle = "Recently Watched",
                        items = recentlyWatchedItems,
                        onSelect = { selectedItem = it }
                    )
                }
            }
        }
    }
}

/** Data class for Home UI state. */
private sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    object Error : HomeUiState()
    data class Content(
        val continueWatching: List<MediaItem> = emptyList(),
        val recentlyAdded: List<MediaItem> = emptyList(),
        val recentlyWatched: List<MediaItem> = emptyList()
    ) : HomeUiState()
}

/**
 * Featured item display at the top of the home screen.
 * Shows a large MediaCard with the featured content.
 */
@Composable
fun FeaturedItem(
    mediaItem: MediaItem,
    onSelect: () -> Unit,
    focusRequester: FocusRequester
) {
    var isFocused by remember { mutableStateOf(false) }

    SkyBoxTvTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .maxHeight(250.dp)
                .focusRequester(focusRequester)
                .background(
                    color = colorScheme.background,
                    shadow = androidx.compose.ui.shadow.Shadow(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f),
                        spread = 2f,
                        radius = 8f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterStart
            ) {
                // Thumbnail - larger for featured
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .maxHeight(180.dp)
                        .background(androidx.compose.ui.graphics.Color.Black)
                ) {
                    // Placeholder for video thumbnail
                    Text(
                        text = "${mediaItem.fileName.first()}${mediaItem.fileName.last()}",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f)),
                        style = MaterialTheme.typography.body2,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                }

                spacer(height = 12.dp)

                // Title
                Text(
                    text = mediaItem.fileName,
                    style = MaterialTheme.typography.h5,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                spacer(height = 8.dp)

                // Subtitle - duration or source
                Text(
                    text = "${mediaItem.durationMs / 60000}m video",
                    style = MaterialTheme.typography.body1,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                spacer(height = 16.dp)

                // Select button
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Watch Now")
                        .style(MaterialTheme.typography.button)
                }
            }
        }
    }
}

/**
 * Factory function to provide a MediaCatalogRepository instance.
 * In production, this would return a RoomWatchProgressRepository or similar.
 * For now, returns a RoomMediaCatalogRepository with the app database.
 */
private fun roomMediaCatalogRepository(): MediaCatalogRepository {
    // This would normally be provided via Hilt/DI
    // For now, we'll use a placeholder - the actual implementation
    // would require a SkyBoxDatabase instance
    throw RuntimeError("MediaCatalogRepository not configured - use with DI")
}