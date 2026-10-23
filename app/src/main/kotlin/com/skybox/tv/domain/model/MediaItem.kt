/**
 * Basic media item model representing a video file from Telegram.
 * Contains the essential metadata needed for playback and display.
 */
package com.skybox.tv.domain.model

data class MediaItem(
    /** Unique media identifier (TDLib internal ID) */
    val mediaId: Long,
    /** Chat identifier where the message was sent */
    val chatId: Long,
    /** Message identifier */
    val messageId: Long,
    /** File name as provided by Telegram */
    val fileName: String,
    /** MIME type (e.g., "video/mp4", "video/mkv") */
    val mimeType: String,
    /** File size in bytes */
    val sizeBytes: Long,
    /** Duration in milliseconds (for video files) */
    val durationMs: Long
) : Comparable<MediaItem> {
    override compareTo(other: MediaItem): Int {
        return mediaId.compareTo(other.mediaId)
    }
}