/**
 * PlaybackController defines the interface for controlling media playback.
 * This abstraction allows the UI to control playback without knowing
 * the underlying implementation (ExoPlayer or other).
 *
 * Pattern: Controller interface following clean architecture principles.
 * Implementations: ExoPlayerPlaybackController, TestingPlaybackController, etc.
 */
package com.skybox.tv.playback

/**
 * Interface for playback control operations.
 */
interface PlaybackController {

    /** Flow of playback state for UI observation. */
    val stateFlow: StateFlow<PlaybackState>

    /** Whether playback is currently active (not Idle). */
    val isPlaying: Boolean
        get() = stateFlow.value !is Idle

    /** Whether playback is paused. */
    val isPaused: Boolean
        get() = stateFlow.value == PlaybackState.Paused

    /** Whether playback is buffering. */
    val isBuffering: Boolean
        get() = stateFlow.value == PlaybackState.Buffering

    /** Whether user is seeking. */
    val isSeeking: Boolean
        get() = stateFlow.value == PlaybackState.Seeking

    /** Whether an error state is active. */
    val hasError: Boolean
        get() = stateFlow.value is PlaybackState.Error

    /** Current error message, if any. */
    val errorMessage: String?
        get() = when (stateFlow.value) {
            is PlaybackState.Error -> stateFlow.value.message
            else -> null
        }

    /** Duration of the media in milliseconds. */
    val durationMs: Long
        get()

    /** Current playback position in milliseconds. */
    val currentPositionMs: Long
        get()

    /** Current playback speed. */
    val speed: Float
        get()

    /** Start playback of a media item. */
    fun play(mediaItem: MediaItem, dataSource: DataSource)

    /** Pause current playback. */
    fun pause()

    /** Resume playback from paused state. */
    fun resume()

    /** Seek to a specific position (in milliseconds). */
    fun seekTo(positionMs: Long)

    /** Stop playback and reset the session. */
    fun stop()

    /** Set playback speed. */
    fun setSpeed(speed: Float)

    /** Release player resources. */
    fun release()
}

/**
 * Data class representing a media item for playback control.
 * Wraps the domain model MediaItem with playback-specific metadata.
 */
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
    val durationMs: Long,
    /** Optional current position when playback was paused */
    val currentPositionMs: Long = 0,
    /** Optional playback speed */
    val speed: Float = 1.0f
)

/**
 * Data class representing a data source for playback.
 * Abstracts the TelegramMediaDataSource behind a simpler interface.
 */
data class DataSource(
    /** URI or identifier for the media source */
    val uri: Any?,
    /** Optional subtitle track index */
    val subtitleTrackIndex: Int = 0,
    /** Optional audio track index */
    val audioTrackIndex: Int = 0
)