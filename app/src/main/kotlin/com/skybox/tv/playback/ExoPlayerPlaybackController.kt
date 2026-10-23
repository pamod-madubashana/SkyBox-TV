/**
 * ExoPlayerPlaybackController implements PlaybackController using AndroidX Media3 ExoPlayer.
 * This is the concrete implementation that bridges ExoPlayer to the playback controller interface.
 *
 * Pattern: Strategy pattern - conforms to PlaybackController interface,
 * allowing swap between different player implementations.
 *
 * Dependencies: AndroidX Media3 ExoPlayer, custom DataSource (TelegramMediaDataSource)
 */
package com.skybox.tv.playback

import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioAttributes
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.Builder
import androidx.media3.exoplayer.TrackSelection
import androidx.media3.exoplayer.TrackSelector
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector.Builder
import androidx.media3.common.util.UnstableApi
import com.skybox.tv.domain.model.MediaItem as DomainMediaItem
import com.skybox.tv.data.streaming.TelegramMediaDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Concrete implementation of PlaybackController using ExoPlayer.
 *
 * Responsibilities:
 * - Create and configure ExoPlayer instance
 * - Connect TelegramMediaDataSource to Media3's player pipeline
 * - Handle playback state transitions and emit them via StateFlow
 * - Manage audio and subtitle track selection
 * - Handle errors and recovery
 * - Coordinate with ProgressTracker for position saving
 *
 * Lifecycle: Player is created on first play(), released on release().
 * Thread safety: All ExoPlayer operations should be called on the main thread
 * or the player's internal thread.
 */
class ExoPlayerPlaybackController(
    private val context: android.content.Context,
    private val telegramMediaDataSource: TelegramMediaDataSource
) : PlaybackController {

    /** Flow of playback state for UI observation. */
    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val stateFlow: StateFlow<PlaybackState> = _state

    /** The underlying ExoPlayer instance. */
    private var exoPlayer: ExoPlayer? = null

    /** Factory for creating DataSource instances. */
    private val dataSourceFactory: androidx.media3.upstream.DataSource.Factory get() = run {
        androidx.media3.upstream.DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "SkyBox-TV"),
            telegramMediaDataSource
        )
    }

    /** Track selector for audio and subtitle tracks. */
    private val trackSelector: TrackSelector get() = Builder(dataSourceFactory)
        .setTrackSelectionSelector(AdaptiveTrackSelection(dataSourceFactory))
        .build()

    /**
     * Initialize the player. Must be called before play().
     * Creates the ExoPlayer instance with appropriate configurations.
     */
    fun initialize() {
        exoPlayer = Builder(context)
            .setTrackSelector(trackSelector)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setVideoAttributes(
                androidx.media3.video.VideoAttributes.Builder()
                    .setFrameRate(30f)
                    .build()
            )
            .build()

        // Set up playback state listener
        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateStateFromExoPlayer()
            }

            override fun onPlaybackError(error: Exception?) {
                _state.value = PlaybackState.Error(
                    error?.message ?: "Unknown playback error"
                )
            }

            override fun onReady() {
                updateStateFromExoPlayer()
            }

            override fun onBufferingStart() {
                _state.value = PlaybackState.Buffering
            }

            override fun onBufferingEnd() {
                // Return to previous state (Playing or Paused)
                updateStateFromExoPlayer()
            }
        })
    }

    /** Update the PlaybackState based on ExoPlayer's current state. */
    private fun updateStateFromExoPlayer() {
        when {
            exoPlayer?.isPlaying == true -> _state.value = PlaybackState.Playing
            exoPlayer?.isPaused == true -> _state.value = PlaybackState.Paused
            else -> _state.value = PlaybackState.Idle
        }
    }

    /**
     * Start playback of a media item from Telegram.
     *
     * @param mediaItem The media item to play
     * @param dataSource The TelegramMediaDataSource providing byte access
     */
    override fun play(mediaItem: MediaItem, dataSource: DataSource) {
        // Ensure player is initialized
        if (exoPlayer == null) {
            initialize()
        }

        // Build MediaItem for ExoPlayer using our custom DataSource
        val mediaItemExo = buildExoMediaItem(mediaItem, dataSource)

        // Set the media item and prepare
        exoPlayer?.setMediaItem(mediaItemExo)
        exoPlayer?.prepare()

        // Start playback
        exoPlayer?.start()

        // Emit initial state
        _state.value = PlaybackState.Playing
    }

    /** Build a MediaItem that bridges TelegramMediaDataSource to ExoPlayer. */
    private fun buildExoMediaItem(
        mediaItem: MediaItem,
        dataSource: DataSource
    ): MediaItem {
        val uri = when (dataSource.uri) {
            is TelegramMediaDataSource.Uri -> "telegram://media/${dataSource.uri.mediaId}"
            else -> "${dataSource.uri}"
        }

        return MediaItem.fromUri(uri)
            .setDataSourceFactory(dataSourceFactory)
            .setAudioAttributes(
                androidx.media3.common.audio.AudioAttributes.Builder()
                    .setContentType(
                        androidx.media3.common.audio.AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .build()
            )
            .setVideoAttributes(
                androidx.media3.video.VideoAttributes.Builder()
                    .setFrameRate(30f)
                    .build()
            )

        // Apply track selections from the dataSource if specified
        if (dataSource.subtitleTrackIndex > 0) {
            // Subtitle track selection handled separately via TrackSelector
        }
        if (dataSource.audioTrackIndex > 0) {
            // Audio track selection handled separately via TrackSelector
        }

        return MediaItem.fromUri(uri).setDataSourceFactory(dataSourceFactory)
    }

    /** Pause current playback. */
    override fun pause() {
        exoPlayer?.pause()
        _state.value = PlaybackState.Paused
    }

    /** Resume playback from paused state. */
    override fun resume() {
        exoPlayer?.start()
        _state.value = PlaybackState.Playing
    }

    /** Seek to a specific position (in milliseconds). */
    override fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _state.value = PlaybackState.Seeking
    }

    /** Stop playback and reset the session. */
    override fun stop() {
        exoPlayer?.stop()
        _state.value = PlaybackState.Idle
    }

    /** Set playback speed. */
    override fun setSpeed(speed: Float) {
        exoPlayer?.setSpeed(speed)
    }

    /** Release player resources. */
    override fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _state.value = PlaybackState.Idle
    }

    /** Get current playback position in milliseconds. */
    override val currentPositionMs: Long
        get() = exoPlayer?.currentPosition?.toLong() ?: 0L

    /** Get total duration of the media in milliseconds. */
    override val durationMs: Long
        get() = exoPlayer?.duration?.toLong() ?: 0L

    /** Get current playback speed. */
    override val speed: Float
        get() = when (exoPlayer?.speed) {
            null -> 1.0f
            else -> exoPlayer.speed
        }
}