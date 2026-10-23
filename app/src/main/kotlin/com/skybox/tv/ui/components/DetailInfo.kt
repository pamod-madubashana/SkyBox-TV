package com.skybox.tv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter painterResource
import androidx.compose.ui.image.rememberImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skybox.tv.domain.model.MediaItem

/**
 * Reusable detail information display for a media item.
 * Shows filename/title, duration, size, media type, and source chat.
 * Designed for TV focus-based navigation.
 *
 * @param mediaItem The media item to display details for
 * @param showThumbnail Whether to show the thumbnail image
 */
@Composable
fun DetailInfo(
    mediaItem: MediaItem,
    showThumbnail: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.Start,
        horizontalAlignment = Alignment.Start
    ) {
        // Thumbnail (if available)
        if (showThumbnail) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(80.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder thumbnail - in real app would load from Telegram
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Thm",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }

        // Title / File name
        Text(
            text = mediaItem.fileName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .maxLines(2)
                .overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )

        spacer(height = 4.dp)

        // Info row with duration, size, and media type
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Duration
            DetailInfoRow(
                icon = Icons.Outline.AccessTime,
                label = formatDuration(mediaItem.durationMs),
                contentDescription = "Duration"
            )

            // Size
            DetailInfoRow(
                icon = Icons.Outline.FileDownload,
                label = formatSize(mediaItem.sizeBytes),
                contentDescription = "Size"
            )
        }

        // Media type
        DetailInfoRow(
            icon = when (mediaItem.mimeType) {
                startsWith -> "video/" -> Icons.Outline.VideoLibrary
                else -> Icons.Outline.Description
            },
            label = mediaItem.mimeType,
            contentDescription = "Media type: ${mediaItem.mimeType}"
        )

        // Source chat
        DetailInfoRow(
            icon = Icons.Outline.Chat,
            label = "Chat ${mediaItem.chatId}",
            contentDescription = "Source chat"
        )
    }
}

/** Row displaying a key-value detail pair. */
@Composable
private fun DetailInfoRow(
    icon: androidx.compose.material3.IconVector,
    label: String,
    contentDescription: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .width(20.dp)
                .height(20.dp)
                .contentPadding(4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 4.dp)
                .contentDescription = contentDescription
        )
    }
}

/** Format duration in mm:ss format. */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:$seconds%02d".format(seconds)
}

/** Format size in human-readable format. */
private fun formatSize(sizeBytes: Long): String {
    if (sizeBytes < 1024) {
        return "$sizeBytes B"
    } else if (sizeBytes < 1024 * 1024) {
        return "${(sizeBytes / 1024).toInt()} KB"
    } else {
        return "${((sizeBytes / (1024 * 1024)).toInt)} MB"
    }
}