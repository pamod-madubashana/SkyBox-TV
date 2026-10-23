/**
 * PlaybackSession manages the state of a media playback session.
 *
 * States:
 * - Idle: No playback active
 * - Playing: Media is currently playing
 * - Paused: Media is paused
 * - Buffering: Media is buffering
 * - Error: An error occurred during playback
 */
package com.skybox.tv.presentation.player

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Session state representing the current playback status.
 */
sealed class PlaybackSessionState {

    /** No playback is active or session has not been started. */
    object Idle : PlaybackSessionState()

    /** Media is currently playing. */
    object Playing : PlaybackSessionState()

    /** Media is paused. */
    object Paused : PlaybackSessionState()

    /** Media is buffering - loading more data. */
    object Buffering : PlaybackSessionState()

    /** An error occurred during playback. */
    data class Error(val message: String) : PlaybackSessionState()
}

/**
 * PlaybackSession manages the ExoPlayer lifecycle and state.
 *
 * Exposes a StateFlow<PlaybackSessionState> so UI can observe
 * state changes and react accordingly (show play/pause, hide controls,
 * display error messages, etc.).
 */
class PlaybackSession {

    private val _state = MutableStateFlow<PlaybackSessionState>(PlaybackSessionState.Idle)
    val state: StateFlow<PlaybackSessionState> = _state.asStateFlow()

    // ExoPlayer instance (created lazily)
    private var player: com.google.android.exoplayer.Player? = null

    // Media item currently being played
    private var mediaItem: com.skybox.tv.domain.model.MediaItem? = null

    // Data source for the media
    private var dataSource: com.skybox.tv.data.streaming.TelegramMediaDataSource? = null

    /**
     * Start playback of the given media item.
     *
     * @param mediaItem The media item to play
     * @param dataSource The TelegramMediaDataSource providing byte access
     */
    fun startPlayback(
        mediaItem: com.skybox.tv.domain.model.MediaItem,
        dataSource: com.skybox.tv.data.streaming.TelegramMediaDataSource
    ) {
        this.mediaItem = mediaItem
        this.dataSource = dataSource
        _state.value = PlaybackSessionState.Playing

        // Create ExoPlayer if not already created
        player = com.google.android.exoplayer.ExoPlayer.Builder(
            /* context */ ?? TODO: Context provider needed
        ).build()

        // Build the media item for ExoPlayer
        // In a full implementation, we would use a ProgressiveMediaItem
        // built on top of our TelegramMediaDataSource
        TODO("Build ExoMediaItem from TelegramMediaDataSource")
    }

    /**
     * Pause the current playback.
     */
    fun pausePlayback() {
        player?.pause()
        _state.value = PlaybackSessionState.Paused
    }

    /**
     * Resume playback from paused state.
     */
    fun resumePlayback() {
        player?.resume()
        _state.value = PlaybackSessionState.Playing
    }

    /**
     * Seek to the specified position (in milliseconds).
     *
     * @positionMs Position in milliseconds to seek to
     */
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    /**
     * Get current playback position in milliseconds.
     *
     * @return Current position, or 0 if not playing
     */
    fun getCurrentPositionMs(): Long {
        return player?.currentPosition?.toLong() ?: 0
    }

    /**
     * Get total duration of the media in milliseconds.
     *
     * @return Total duration, or 0 if unknown
     */
    fun getDurationMs(): Long {
        return player?.duration?.toLong() ?: 0
    }

    /**
     * Stop playback and reset the session.
     */
    fun stopPlayback() {
        player?.stop()
        _state.value = PlaybackSessionState.Idle
        mediaItem = null
        dataSource = null
        // Player will be released when no longer needed
    }

    /**
     * Release the ExoPlayer resources.
     * Should be called when the session is no longer needed (e.g., activity destruction).
     */
    fun release() {
        player?.release()
        player = null
        _state.value = PlaybackSessionState.Idle
        mediaItem = null
        dataSource = null
    }
}