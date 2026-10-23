/**
 * LibraryScreen — Compose TV library screen with poster grid.
 * Shows user's media library with filtering and sorting options.
 * Uses MediaCatalogRepository for data source with paging support.
 * Designed for D-pad navigation on Android TV.
 */
package com.skybox.tv.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybox.tv.data.catalog.MediaCatalogRepository
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.components.*
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme
import kotlin.coroutines.isActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow flow {
    it.collect()
}
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Library screen showing user's media collection with filtering and sorting.
 * Features:
 * - Poster/card grid layout appropriate for TV
 * - Sorting by name, date, duration
 * - Filtering by media type and source chat
 * - Paging/incremental loading
 * - Loading, empty, offline, error states
 */
@Composable
fun LibraryScreen(
    repository: MediaCatalogRepository = roomMediaCatalogRepository(),
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    // UI State
    var selectedFilter by remember { mutableStateOf(LibraryFilter.All) }
    var sortOption by remember { mutableStateOf(LibrarySort.ByName) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter enum
    enum class LibraryFilter {
        All,
        Videos,
        Images,
        Documents
    }

    enum class LibrarySort {
        ByName,
        ByDate,
        ByDuration
    }

    // State for loading/empty/error
    val libraryState by remember {
        mutableStateOf(LibraryUiState.Loading)
    }

    // Paged list of media items
    val pagedItems by remember {
        mutableStateOf<List<MediaItem>>(emptyList())
    }

    // Collect paged data from repository
    LaunchedEffect(Unit) {
        // Get items filtered and sorted
        val filteredItems = when (selectedFilter) {
            LibraryFilter.All -> repository.getAllPaged()
            LibraryFilter.Videos -> repository.getByAvailability("available")
            LibraryFilter.Images -> repository.getByAvailability("image")
            LibraryFilter.Documents -> repository.getByAvailability("document")
            else -> repository.getAllPaged()
        }

        filteredItems.collectAsStateWithLifecycle(
            lifecycle = androidx.lifecycle.LifecycleOwner.current.lifecycle,
            initialValue = emptyList()
        ) { items ->
            // Apply sorting
            val sortedItems = when (sortOption) {
                LibrarySort.ByName -> items.sortedBy { it.fileName }
                LibrarySort.ByDate -> items.sortedBy { it.dateAdded ?: 0L }
                LibrarySort.ByDuration -> items.sortedBy { it.durationMs }
                else -> items
            }
            libraryState = when {
                items.isEmpty() && libraryState !in listOf(LibraryUiState.Error) -> LibraryUiState.Empty
                items.isNotEmpty() -> LibraryUiState.Content
                else -> libraryState
            }
        }
    }

    SkyBoxTvTheme {
        when (libraryState) {
            is LibraryUiState.Loading -> {
                LoadingState(
                    message = "Loading library...",
                    focusRequester = focusRequester
                )
            }

            is LibraryUiState.Empty -> {
                EmptyState(
                    title = "Empty Library",
                    description = "No media found.\nApply filters or add videos from your Telegram chats.",
                    actionText = "Browse Chats",
                    onAction = { /* Navigate to chats */ },
                    focusRequester = focusRequester
                )
            }

            is LibraryUiState.Error -> {
                ErrorState(
                    message = "Failed to load library",
                    onRetry = { /* reload data */ },
                    focusRequester = focusRequester
                )
            }

            is LibraryUiState.Content -> {
                libraryContentArea(
                    items = pagedItems,
                    selectedFilter = selectedFilter,
                    sortOption = sortOption,
                    searchQuery = searchQuery,
                    onFilterChanged = { selectedFilter = it },
                    onSortChanged = { sortOption = it },
                    onSearchChanged = { searchQuery = it },
                    focusRequester = focusRequester
                )
            }
        }
    }
}

/** Data class for Library UI state. */
private sealed class LibraryUiState {
    object Loading : LibraryUiState()
    object Empty : LibraryUiState()
    object Error : LibraryUiState()
    data class Content(
        val items: List<MediaItem> = emptyList(),
        val pagingInfo: androidx.paging.ComposePagingInfo? = null
    ) : LibraryUiState()
}

/**
 * Main library content area with filter/sort controls and item grid.
 */
@Composable
fun libraryContentArea(
    items: List<MediaItem>,
    selectedFilter: LibraryFilter,
    sortOption: LibrarySort,
    searchQuery: String,
    onFilterChanged: (LibraryFilter) -> Unit,
    onSortChanged: (LibrarySort) -> Unit,
    onSearchChanged: (String) -> Unit,
    focusRequester: FocusRequester
) {
    // Control bar at top
    var searchFocus by remember { mutableStateOf(false) }

    SkyBoxTvTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Start,
            horizontalAlignment = Alignment.Start
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .focusRequester(focusRequester),
                keyboardOptions = androidx.compose.ui.input.keyboard.KeyboardOptions(
                    keyboardType = androidx.compose.ui.input.keyboard.KeyboardType.Text
                ),
                predicate = { it.length <= 50 },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Button(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text("Clear", style = MaterialTheme.typography.caption)
                        }
                    } else {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material3.Icons.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )

            spacer(height = 16.dp)

            // Filter options row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedFilter == LibraryFilter.All,
                    onSelected = { onFilterChanged(LibraryFilter.All) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == LibraryFilter.Videos,
                    onSelected = { onFilterChanged(LibraryFilter.Videos) },
                    label = { Text("Videos") }
                )
                FilterChip(
                    selected = selectedFilter == LibraryFilter.Images,
                    onSelected = { onFilterChanged(LibraryFilter.Images) },
                    label = { Text("Images") }
                )
                FilterChip(
                    selected = selectedFilter == LibraryFilter.Documents,
                    onSelected = { onFilterChanged(LibraryFilter.Documents) },
                    label = { Text("Docs") }
                )
            }

            spacer(height = 16.dp)

            // Sort options row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SortChip(
                    selected = sortOption == LibrarySort.ByName,
                    onSelected = { onSortChanged(LibrarySort.ByName) },
                    label = { Text("Name") }
                )
                SortChip(
                    selected = sortOption == LibrarySort.ByDate,
                    onSelected = { onSortChanged(LibrarySort.ByDate) },
                    label = { Text("Date") }
                )
                SortChip(
                    selected = sortOption == LibrarySort.ByDuration,
                    onSelected = { onSortChanged(LibrarySort.ByDuration) },
                    label = { Text("Duration") }
                )
            }

            spacer(height = 24.dp)

            // Item grid - rows of media cards
            if (items.isEmpty()) {
                EmptyState(
                    title = "No Items",
                    description = "No media matches your filter criteria.\nTry changing the filter or search term.",
                    focusRequester = focusRequester
                )
            } else {
                // Grid of media items in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    navigationMode = androidx.compose.ui.NavigationMode.Horizontal
                ) {
                    items.forEachIndexed { index, mediaItem ->
                        MediumGridItem(
                            mediaItem = mediaItem,
                            onSelect = { /* Navigate to player */ },
                            focusRequester = if (index == 0) focusRequester else null
                        )
                        if (index < items.lastIndex) {
                            spacer(horizontal = 16.dp)
                        }
                    }
                }

                // Show count if many items
                if (items.size > 6) {
                    spacer(height = 8.dp)
                    Text(
                        text = "Showing ${items.size} items",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/** Filter chip UI component. */
@Composable
fun FilterChip(
    selected: Boolean,
    onSelected: () -> Unit,
    label: String
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(
                if (selected) androidx.compose.ui.graphics.Color.Cyan else androidx.compose.ui.graphics.Color.Transparent,
                shape = androidx.compose.ui.shape.RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(8.dp),
            color = if (selected) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Gray
        )
    }
}

/** Sort chip UI component. */
@Composable
fun SortChip(
    selected: Boolean,
    onSelected: () -> Unit,
    label: String
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(
                if (selected) androidx.compose.ui.graphics.Color.Magenta else androidx.compose.ui.graphics.Color.Transparent,
                shape = androidx.compose.ui.shape.RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(8.dp),
            color = if (selected) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.Gray
        )
    }
}

/**
 * MediumGridItem — Medium-sized poster card for library grid.
 * Displays a media item in the library grid layout.
 */
@Composable
fun MediumGridItem(
    mediaItem: MediaItem,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    SkyBoxTvTheme {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .maxWidth(130.dp)
                .minHeight(180.dp)
                .focusRequester(focusRequester)
                .clickable {
                    onSelect()
                }
                .background(androidx.compose.ui.graphics.Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "${mediaItem.fileName.first()}${mediaItem.fileName.last()}",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.body1,
                    contentScale = ContentScale.Fit,
                    textAlign = androidx.compose.ui.Alignment.Center
                )
            }

            // Title below image
            spacer(height = 8.dp)
            Text(
                text = mediaItem.fileName,
                style = MaterialTheme.typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = androidx.compose.ui.Alignment.Center
            )
        }
    }
}

/**
 * Factory function to provide a MediaCatalogRepository instance.
 * In production, this would return a RoomMediaCatalogRepository with the app database.
 */
private fun roomMediaCatalogRepository(): MediaCatalogRepository {
    // This would normally be provided via Hilt/DI
    // For now, we'll use a placeholder - the actual implementation
    // would require a SkyBoxDatabase instance
    throw RuntimeError("MediaCatalogRepository not configured - use with DI")
}