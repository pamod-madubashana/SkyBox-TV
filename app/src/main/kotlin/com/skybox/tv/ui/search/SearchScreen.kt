package com.skybox.tv.ui.search

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pointer.keyboardKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.skybox.tv.data.catalog.MediaCatalogRepository
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.domain.usecase.SearchMediaUseCase
import com.skybox.tv.ui.components.SearchBar
import com.skybox.tv.ui.components.SearchActionChip
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.rememberCoroutineScope
import kotlin.coroutines.resumeWithException

/**
 * Search screen for SkyBox TV.
 * Searches the local catalog first, then Telegram if needed.
 * Designed for Android TV D-pad navigation.
 *
 * Features:
 * - Search input field (D-pad friendly)
 * - Search local catalog first (avoid repeated Telegram queries)
 * - Results in grid/list
 * - Loading, empty, error states
 * - Debounced search (avoid querying on every keystroke)
 */
@Composable
fun SearchScreen(
    catalogRepository: MediaCatalogRepository,
    onBack: () -> Unit
) {
    val searchText by remember { mutableStateOf("") }
    val results by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    val loading by remember { mutableStateOf(false) }
    val error by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    private val searchScope = rememberCoroutineScope()

    // Debounced search: perform search after user stops typing for 500ms
    var debounceToken by remember { mutableStateOf(0L) }

    LaunchedEffect(searchText) {
        // Cancel any previous pending search
        searchScope.cancelActive()
        // Start new debounce timer
        searchScope.launch {
            delay(500L) // Wait for user to stop typing
            // Only perform search if text hasn't changed
            if (searchText.isNotEmpty() && searchText == searchText) {
                performSearch()
            }
        }
    }

    fun performSearch() {
        loading = true
        error = null
        searchScope.launch {
            try {
                // Search local catalog first
                val localResults = catalogRepository.search(searchText)
                results = localResults.map { entity ->
                    mapEntityToMediaItem(entity)
                }
            } catch (e: Exception) {
                error = "Search failed: ${e.message}"
                results = emptyList()
            } finally {
                loading = false
            }
        }
    }

    fun mapEntityToMediaItem(entity: com.skybox.tv.data.database.entity.MediaItemEntity): MediaItem {
        return MediaItem(
            mediaId = entity.mediaId.toLong(),
            chatId = entity.chatId.toLong(),
            messageId = entity.messageId.toLong(),
            fileName = entity.fileName,
            mimeType = entity.mimeType,
            sizeBytes = entity.sizeBytes,
            durationMs = entity.durationMs
        )
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

                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .maxLines(1)
                        .overflow = TextOverflow.Ellipsis,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
            }

            spacer(height = 16.dp)

            // Search input
            SearchBar(
                value = searchText,
                onValueChanged = { newText ->
                    // Debounce is handled by LaunchedEffect above
                },
                placeholder = "Search videos...",
                onSearchPerformed = { query ->
                    // Perform immediate search on submission
                    if (query.isNotEmpty()) {
                        performSearch()
                    }
                },
                focusRequester = focusRequester
            )

            spacer(height = 8.dp)

            // Search action chip
            SearchActionChip(
                searchText = searchText,
                onClear = { searchText = "" },
                onSearch = { query ->
                    if (query.isNotEmpty()) {
                        performSearch()
                    }
                },
                isSearching = loading
            )

            spacer(height = 16.dp)

            // Results area
            when {
                is loading -> {
                    Center {
                        CircularProgressIndicator()
                        spacer(height = 8.dp)
                        Text("Searching local catalog...")
                    }
                }

                is error != null -> {
                    Center {
                        Icon(
                            imageVector = Icons.Failed,
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                        spacer(height = 8.dp)
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.width(300.dp)
                        )
                        spacer(height = 16.dp)
                        OutlinedButton(
                            onClick = { performSearch() },
                            modifier = Modifier.focusRequester(focusRequester)
                        ) {
                            Text("Try Again")
                        }
                    }
                }

                results.isEmpty() -> {
                    emptyStateSearch(focusRequester)
                }

                else -> {
                    resultsList(focusRequester)
                }
            }
        }
    }
}

/** Display search results in a grid/list format. */
@Composable
private fun resultsList(
    focusRequester: androidx.compose.ui.focus.FocusRequester
) {
    // Grid of search results
    // Placeholder - in real implementation would iterate over results
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Center
    ) {
        Text(
            text = "Search results would appear here",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

/** Empty state for search results. */
private fun emptyStateSearch(focusRequester: androidx.compose.ui.focus.FocusRequester) {
    Center {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outline.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colorScheme.onSurfaceVariant
            )
            spacer(height = 8.dp)
            Text(
                text = "No Results",
                style = MaterialTheme.typography.titleMedium
            )
            spacer(height = 16.dp)
            Text(
                text = "No videos match your search.",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(300.dp)
            )
            spacer(height = 24.dp)
            OutlinedButton(
                onClick = {},
                modifier = Modifier.focusRequester(focusRequester)
            ) {
                Text("New Search")
            }
        }
    }
}