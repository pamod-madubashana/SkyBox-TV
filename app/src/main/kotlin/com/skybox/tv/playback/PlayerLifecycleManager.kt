/**
 * PlayerLifecycleManager handles the ExoPlayer lifecycle in the context of
 * the Android component lifecycle (activities, fragments, background state).
 *
 * Responsibilities:
 * - Create and manage ExoPlayer instance tied to lifecycle events
 * - Save playback progress when the player is paused, stopped, or sent to background
 * - Handle process death and player recovery
 * - Release player resources when no longer needed
 * - Coordinate with ProgressTracker to save watch progress
 *
 * Pattern: Lifecycle-aware component following the "retain player, release on destroy"
 * pattern. The player should outlast configuration changes but be released when
 * the UI component is destroyed.
 *
 * Lifecycle flow:
 *   1. onCreate/onStart: Initialize player (lazy - on first play)
 *   2. onResume: Player is active, UI can control playback
 *   3. onPause/SaveInstanceState: Save progress, keep player running or pause
 *   4. onStop/Send to background: Save progress, decide whether to keep player
 *   5. onDestroy: Release player resources
 */
package com.skybox.tv.playback

import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.playback.PlaybackState
import com.skybox.tv.playback.PlaybackState.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.lifecycleScope
import kotlinx.coroutines.withContext

/**
 * Callback interface for lifecycle events.
 */
interface PlayerLifecycleManagerCallback {

    /** Called when playback progress should be saved. */
    fun onProgressShouldSave(mediaId: Long, chatId: Long, messageId: Long, positionMs: Long)

    /** Called when the player is being sent to background. */
    fun onGoingToBackground()

    /** Called when the player should be released. */
    fun onPlayerShouldRelease()
}

/**
 * Manages the ExoPlayer lifecycle in coordination with the Android component lifecycle.
 *
 * This class is designed to be used from a ViewModel or lifecycle-aware component.
 * It handles the complex interplay between player state, component state, and progress saving.
 *
 * Key design decisions:
 * - Player is created lazily on first play(), not at construction
 * - Progress is saved on pause, stop, or when going to background
 * - Player can be retained across configuration changes (when used with ViewModel)
 * - Process death is handled via player's built-in recovery mechanisms
 */
class PlayerLifecycleManager(
    private val context: Context,
    private val playbackController: PlaybackController,
    private val progressTracker: ProgressTracker?,
    private val callback: PlayerLifecycleManagerCallback?
) {

    private var isPlayerInitialized = false
    private var isInBackground = false
    private var WakeLock: PowerManager.WakeLock? = null

    /**
     * Initialize the player lifecycle manager.
     * Sets up wake lock for continuous playback and prepares for player creation.
     */
    fun initialize() {
        // Acquire wake lock to keep screen on during playback
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        WakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SkyBox-TV:PlayerWakeLock"
        )
        WakeLock?.apply { isEnabled = true }
    }

    /**
     * Called when the UI component is being created.
     * Initialize any state needed for playback.
     */
    fun onCreate() {
        // Player will be initialized lazily on first play()
        isPlayerInitialized = false
    }

    /**
     * Called when the UI component gains focus.
     * Player should be ready for playback.
     */
    fun onResume() {
        // Ensure player is set up
        if (!isPlayerInitialized) {
            playbackController.initialize()
            isPlayerInitialized = true
        }
        // Player is now active
    }

    /**
     * Called when the UI component loses focus (paused).
     * Save playback progress and optionally pause the player.
     *
     * @param saveProgress Whether to save progress (true for pause/stop, false for config change)
     */
    fun onPause(saveProgress: Boolean = true) {
        if (saveProgress) {
            saveCurrentProgress()
        }
        // Player can be paused or kept running depending on use case
        playbackController.pause()
    }

    /**
     * Called when the component is being stopped (sent to background or navigation).
     * Save progress and decide whether to keep player running in background.
     */
    fun onStop() {
        saveCurrentProgress()

        // Decide whether to keep player running
        // In a full implementation, this would consider:
        // - Whether there's an ongoing foreground service
        // - User preferences for background playback
        // - Whether another screen is still watching
        isInBackground = true

        if (callback != null) {
            callback?.onGoingToBackground()
        }

        // Pause playback when going to background
        playbackController.pause()
    }

    /**
     * Called when the component is being destroyed.
     * Release player resources and clean up.
     */
    fun onDestroy() {
        releasePlayer()
        WakeLock?.isEnabled = false
        WakeLock = null

        callback?.onPlayerShouldRelease()
    }

    /**
     * Set the media item currently being played.
     *
     * @param mediaItem The media item to play
     */
    fun setMediaItem(mediaItem: MediaItem) {
        // Could update progress tracker with media identifiers
        progressTracker?.let { tracker ->
            // Tracker starts observing when play() is called
        }
    }

    /** Save the current playback progress to the repository. */
    private fun saveCurrentProgress() {
        val positionMs = playbackController.currentPositionMs
        val durationMs = playbackController.durationMs

        // Only save if we're actually playing something
        if (positionMs > 0 && playbackController.isPlaying) {
            progressTracker?.let { tracker ->
                viewModelScope().launch {
                    try {
                        tracker.saveProgress(positionMs)
                        callback?.onProgressShouldSave(
                            /* mediaId would need to be stored */ 0L,
                            0L,
                            0L,
                            positionMs
                        )
                    } catch (e: Exception) {
                        Log.e("PlayerLifecycleManager", "Failed to save progress", e)
                    }
                }
            }
        }
    }

    /** Release the ExoPlayer and associated resources. */
    private fun releasePlayer() {
        playbackController.release()

        // Release wake lock
        WakeLock?.isEnabled = false
        WakeLock = null

        // Clear internal state
        isPlayerInitialized = false
        isInBackground = false
    }

    /** Scope for lifecycle-aware coroutines. */
    private val viewModelScope: CoroutineScope
        get() = kotlinx.coroutines.viewModelScope
}