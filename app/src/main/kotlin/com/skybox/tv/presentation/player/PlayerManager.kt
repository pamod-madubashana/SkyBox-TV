/**
 * PlayerManager manages the ExoPlayer lifecycle for SkyBox TV.
 *
 * Responsibilities:
 * - Create and configure ExoPlayer instances
 * - Connect TelegramMediaDataSource to Media3's player pipeline
 * - Handle playback state transitions (play, pause, stop)
 * - Manage error handling and recovery
 * - Coordinate with PlaybackSession for state tracking
 *
 * Pattern: Player lifecycle is independent of UI screens.
 * The PlayerManager can be retained across configuration changes
 * while UI components observe its state via PlaybackSession.
 */
package com.skybox.tv.presentation.player

import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer.Player
import com.google.android.exoplayer.ExoPlayer
import com.google.android.exoplayer.MediaItem
import com.google.android.exoplayer.audio.AudioAttributes
import com.google.android.exoplayer.video.VideoAttributes
import com.google.android.exoplayer.upstream.DataSpec
import com.google.android.exoplayer.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * PlayerManager handles the creation and lifecycle of ExoPlayer.
 *
 * In a full implementation, PlayerManager would:
 * - Use a DefaultDataSourceFactory with a custom DataSource (TelegramMediaDataSource)
 * - Build a ProgressiveMediaItem that reads from the custom DataSource
 * - Handle audio/subtitle track selection
 * - Save and restore playback position
 */
class PlayerManager {

    private val playbackSession = PlaybackSession()

    /** Flow of playback session state for UI observation. */
    val sessionState: StateFlow<PlaybackSession.PlaybackSessionState> =
        playbackSession.state

    /** The underlying ExoPlayer instance. */
    private var exoPlayer: ExoPlayer? = null

    /** Factory for creating DataSource instances from TelegramMediaDataSource. */
    private val dataSourceFactory: DefaultDataSourceFactory get() = run {
        // In a full implementation, this would use:
        // DefaultDataSourceFactory(context, telegramMediaDataSource)
        // For now, we use a placeholder that will be connected
        // when the UI layer provides the application Context
        throw IllegalStateException(
            "PlayerManager not initialized. Call initialize(context) first."
        )
    }

    /**
     * Initialize the PlayerManager with an Android Context.
     *
     * @param context Application context needed for DataSource factory creation
     */
    fun initialize(context: android.content.Context) {
        // Player initialization will happen when playback starts
        // The ExoPlayer will be created on-demand
    }

    /**
     * Start playback of a media item from Telegram.
     *
     * @param mediaItem The MediaItem containing Telegram file metadata
     * @param dataSource The TelegramMediaDataSource providing byte access
     * @param context Application Context for DataSource factory
     */
    fun play(
        mediaItem: com.skybox.tv.domain.model.MediaItem,
        dataSource: com.skybox.tv.data.streaming.TelegramMediaDataSource,
        context: android.content.Context
    ) {
        // Update the data source with TDLib file information if available
        // This would be called from the repository when file downloads progress

        // Create ExoPlayer if not already created
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setVideoAttributes(
                    VideoAttributes.Builder()
                        .setFrameRate(30f)
                        .build()
                )
                .build()
        }

        // Build a MediaItem that uses our custom DataSource
        // This is the key integration point between TDLib and ExoPlayer
        val mediaItemExo = buildExoMediaItem(mediaItem, dataSource, context)

        // Set the media item and start playback
        exoPlayer?.setMediaItem(mediaItemExo)
        exoPlayer?.prepare()

        // Set up playback state listener
        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) {
                    _state.value = PlaybackSessionState.Idle
                }
            }

            override fun onPlaybackError(error: Exception?) {
                _state.value = PlaybackSessionState.Error(
                    error?.message ?: "Unknown playback error"
                )
            }

            override fun onReady() {
                // Media is ready to play
                _state.value = PlaybackSessionState.Playing
            }
        })

        // Update session state
        playbackSession.startPlayback(mediaItem, dataSource)
    }

    /**
     * Build a MediaItem that bridges TelegramMediaDataSource to ExoPlayer.
     *
     * In a proof-of-concept implementation, we use a ProgressiveMediaItem
     * with a custom DataSource factory that wraps our TelegramMediaDataSource.
     *
     * @param mediaItem Media metadata (file ID, size, etc.)
     * @param dataSource The TelegramMediaDataSource instance
     * @param context Application Context
     * @return A MediaItem ready for ExoPlayer consumption
     */
    private fun buildExoMediaItem(
        mediaItem: com.skybox.tv.domain.model.MediaItem,
        dataSource: com.skybox.tv.data.streaming.TelegramMediaDataSource,
        context: android.content.Context
    ): MediaItem {
        // Create a DataSpec for the media
        val dataSpec = DataSpec(/* uri */ null) // Will be replaced with Telegram URI

        // Create a DataSource factory that uses our TelegramMediaDataSource
        // This is where the TDLib bridge happens
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.SDK_INT,
            object : DefaultDataSourceFactory.DataSourceFactory {
                override fun createDataSource(): DefaultDataSource.DataSource {
                    // Return our custom TelegramMediaDataSource
                    return dataSource
                }
            }
        )

        // Build a ProgressiveMediaItem using the custom data source
        return MediaItem.fromUri(
            "telegram://media/${mediaItem.mediaId}"
        ).setDataSourceFactory(dataSourceFactory)
    }

    /**
     * Pause current playback.
     */
    fun pause() {
        exoPlayer?.pause()
        playbackSession.pausePlayback()
    }

    /**
     * Resume playback from paused state.
     */
    fun resume() {
        exoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer?.seekTo(playbackSession.getCurrentPositionMs())
        exoPlayer?.start()
        playbackSession.resumePlayback()
    }

    /**
     * Seek to a specific position.
     *
     * @positionMs Position in milliseconds to seek to
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    /**
     * Get current playback position in milliseconds.
     *
     * @return Current position, or 0 if not playing
     */
    fun getCurrentPositionMs(): Long {
        return exoPlayer?.currentPosition?.toLong() ?: playbackSession.getCurrentPositionMs()
    }

    /**
     * Get total duration of the media in milliseconds.
     *
     * @return Total duration, or 0 if unknown
     */
    fun getDurationMs(): Long {
        return exoPlayer?.duration?.toLong() ?: playbackSession.getDurationMs()
    }

    /**
     * Stop playback and reset the session.
     */
    fun stop() {
        exoPlayer?.stop()
        playbackSession.stopPlayback()
    }

    /**
     * Release the ExoPlayer resources.
     * Should be called when the player is no longer needed
     * (e.g., activity destruction or navigation away).
     */
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        playbackSession.release()
    }
}