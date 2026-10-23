/**
 * MediaCard — Compose TV poster card with focus animation.
 * Displays a media poster/thumbnail with title and focuses on selection.
 * Designed for D-pad navigation on Android TV with content rows.
 */
package com.skybox.tv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.image.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.clickable
import androidx.compose.ui.semantics.isFocusable
import androidx.compose.ui.semantics.clickable
import androidx.compose.ui.semantics.interactionSource
import androidx.compose.ui.semantics.interactionSource.composeInteractionSource
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.theme.TvTheme

/**
 * A poster card for displaying a single media item.
 * Shows thumbnail, title, and supports focus selection.
 *
 * @param mediaItem The media item to display
 * @param onSelect Callback when item is selected/clicked
 * @param modifier Modifier for layout customization
 * @param contentScale Scale type for the thumbnail image
 * @param showTitle Whether to show the title below the thumbnail
 */
@Composable
fun MediaCard(
    mediaItem: MediaItem,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showTitle: Boolean = true,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardModifier = modifier
        .fillMaxWidth()
        .maxWidth(140.dp)
        .minHeight(200.dp)
        .focusRequester(focusRequester)
        .then {
            // Focus animation scale
            if (isFocused) {
                remember {
                    object : androidx.compose.animation.AnimationSpec<Float> {
                        override fun calculateState(
                            oldValue: Float,
                            newValue: Float,
                            oldTime: Long,
                            newTime: Long
                        ): Float = newValue
                    }
                }
            }
            this
        }

    Box(
        modifier = cardModifier
            .clickable(
                interactionSource = composeInteractionSource,
                activated = true
            ) {
                onSelect()
            }
            .isFocusable(isFocused)
    ) {
        SkyBoxTvTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .contentScale(contentScale)
                    .padding(4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterStart
            ) {
                // Thumbnail image
                if (mediaItem.mimeType.startsWith("video")) {
                    // For video files, show a placeholder or first frame
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black)
                    ) {
                        // Placeholder - in production would load actual thumbnail
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
                } else {
                    // For non-video, show generic placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Magenta)
                    ) {
                        Text(
                            text = "${mediaItem.fileName.first()}${mediaItem.fileName.last()}",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)),
                            style = MaterialTheme.typography.body2,
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center
                        )
                    }
                }

                if (showTitle) {
                    spacer(height = 8.dp)
                    Text(
                        text = mediaItem.fileName,
                        style = MaterialTheme.typography.body1,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        textAlign = androidx.compose.ui.Alignment.Center
                    )
                }
            }
        }

        // Focus visual feedback
        if (isFocused) {
            // Draw focus rectangle around the card
            val focusRect = rememberDrawRect(
                color = androidx.compose.ui.graphics.Color.Magenta.copy(alpha = 0.3f),
                width = 4f,
                dashInterval = 8f
            )
            // Focus rectangle is drawn by the drawRect composable
            // when the element has focus
        }
    }
}

/** Draw rectangle for focus visual feedback. */
private fun androidx.compose.ui.graphics.Color.drawRect(
    color: Color,
    width: Float,
    dashInterval: Float
): androidx.compose.ui.draw.DrawModifier {
    // This is a simplified focus rect - in production would use
    // androidx.compose.foundation.rememberDrawRect
    return androidx.compose.foundation.rememberDrawRect(
        color = color,
        width = width
    )
}