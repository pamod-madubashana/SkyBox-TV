/**
 * ContentRow — Horizontal scrolling row for TV Compose.
 * Displays a list of media items in a horizontally scrolling row.
 * Designed for D-pad navigation on Android TV with focus progression.
 */
package com.skybox.tv.ui.components

import androidx.compose.foundation.*
import androidx.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MemoryPeeking
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.isFocusable
import androidx.compose.ui.semantics.clickable
import androidx.compose.ui.semantics.interactionSource
import androidx.compose.ui.semantics.interactionSource.composeInteractionSource
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.components.MediaCard
import com.skybox.tv.ui.theme.TvTheme

/**
 * A horizontal scrolling row of media cards.
 * Items are evenly spaced and focus can move between them via D-pad.
 *
 * @param items List of media items to display
 * @param onSelect Callback when an item is selected
 * @param modifier Modifier for layout customization
 * @param verticalSpacing Vertical space between items
 * @param horizontalSpacing Horizontal space between items
 * @param contentScale Scale type for thumbnail images
 * @param focusRequester Focus requester for D-pad navigation
 */
@Composable
fun ContentRow(
    items: List<MediaItem>,
    onSelect: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    verticalSpacing: dp = 8.dp,
    horizontalSpacing: dp = 16.dp,
    contentScale: ContentScale = ContentScale.Crop,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    val scrollState = rememberScrollState()

    SkyBoxTvTheme {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.Center,
            navigationMode = androidx.compose.ui.NavigationMode.Horizontal
        ) {
            items.forEachIndexed { index, mediaItem ->
                MediaCard(
                    mediaItem = mediaItem,
                    onSelect = { onSelect(mediaItem) },
                    contentScale = contentScale,
                    focusRequester = if (index == 0) focusRequester else null
                )
                spacer(horizontal = horizontalSpacing)
            }
        }

        // Highlight focused item
        if (scrollState.isFocused) {
            // Focus is managed by the parent navigation
        }
    }
}

/**
 * ContentRow with section header.
 *
 * @param sectionTitle Title to display above the row
 * @param items List of media items to display
 * @param onSelect Callback when an item is selected
 * @param modifier Modifier for layout customization
 */
@Composable
fun ContentRowWithHeader(
    sectionTitle: String,
    items: List<MediaItem>,
    onSelect: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    verticalSpacing: dp = 8.dp,
    horizontalSpacing: dp = 16.dp,
    contentScale: ContentScale = ContentScale.Crop,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    val scrollState = rememberScrollState()

    SkyBoxTvTheme {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Section header
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            )

            // Horizontal row of items
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center,
                navigationMode = androidx.compose.ui.NavigationMode.Horizontal
            ) {
                items.forEachIndexed { index, mediaItem ->
                    MediaCard(
                        mediaItem = mediaItem,
                        onSelect = { onSelect(mediaItem) },
                        contentScale = contentScale,
                        focusRequester = if (index == 0 || index == items.lastIndex) focusRequester else null
                    )
                    spacer(horizontal = horizontalSpacing)
                }
            }
        }
    }
}