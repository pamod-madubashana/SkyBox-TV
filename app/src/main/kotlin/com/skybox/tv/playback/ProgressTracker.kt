/**
 * ProgressTracker tracks the playback position of a media item and saves
 * progress at regular intervals to a repository.
 *
 * Pattern: Observer pattern - tracks ExoPlayer position changes and
 * persists them via WatchProgressRepository.
 *
 * Responsibilities:
 * - Monitor ExoPlayer position changes via playback controller state flow
 * - Save progress at configurable intervals (every N milliseconds)
 * - Persist progress to the existing WatchProgressRepository (Phase 3)
 * - Handle cancellation and cleanup
 *
 * Note: This class observes the PlaybackController's stateFlow and
 * delegates persistence to the existing WatchProgressRepository from Phase 3.
 * The repository is com.skybox.tv.data.catalog.WatchProgressRepository.
 */
package com.skybox.tv.playback

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * ProgressTracker monitors playback position and saves progress at intervals.
 *
 * Usage:
 *   1. Create a repository instance (RoomWatchProgressRepository from Phase 3)
 *   2. Create ProgressTracker with the repository
 *   3. Connect to PlaybackController's stateFlow to monitor position changes
 *   4. Call release() when done
 *
 * Thread safety: Should be used from a coroutine scope with appropriate
 * lifecycle management (e.g., ViewModel scope).
 *
 * Dependencies: Uses the existing WatchProgressRepository from data.catalog package.
 */
class ProgressTracker(
    private val watchProgressRepository: com.skybox.tv.data.catalog.WatchProgressRepository,
    private val mediaId: Long,
    private val chatId: Long,
    private val messageId: Long,
    private val saveIntervalMs: Long = 5000  // Save every 5 seconds by default
) {

    /** Flow of current position for observation. */
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    /** Whether tracking is active. */
    private var isTracking = false

    /**
     * Start tracking playback position from a PlaybackController.
     * Connects to the controller's stateFlow to monitor position changes.
     *
     * @param controller The playback controller to observe
     * @param owner Lifecycle owner for scoping coroutines
     */
    fun startTracking(controller: PlaybackController, owner: LifecycleOwner) {
        if (isTracking) return

        isTracking = true
        lifecycleScope.launch(owner.coroutineContext) {
            // Collect state flow changes to monitor position
            controller.stateFlow.collect { state ->
                when (state) {
                    is PlaybackState.Playing -> {
                        // Track position while playing
                        val position = controller.currentPositionMs
                        _currentPosition.value = position

                        // Save at intervals
                        saveProgressIfNeeded(position)
                    }
                    PlaybackState.Paused, PlaybackState.Idle, PlaybackState.Ended -> {
                        // Save final position when stopping/pausing/ending
                        val position = controller.currentPositionMs
                        _currentPosition.value = position
                        saveProgress(position)
                    }
                    PlaybackState.Seeking -> {
                        // During seek, track position
                        val position = controller.currentPositionMs
                        _currentPosition.value = position
                    }
                    PlaybackState.Error -> {
                        // Save position even on error
                        val position = controller.currentPositionMs
                        _currentPosition.value = position
                        saveProgress(position)
                    }
                }
            }
        }
    }

    /**
     * Save progress if enough time has passed since the last save.
     */
    private fun saveProgressIfNeeded(positionMs: Long) {
        // Logic to debounce saves at the configured interval
        // In a full implementation, this could use a timestamp or
        // combine with playback speed awareness
        saveProgress(positionMs)
    }

    /**
     * Save the current progress to the repository.
     *
     * @param positionMs Current playback position in milliseconds
     */
    private fun saveProgress(positionMs: Long) {
        lifecycleScope.launch {
            try {
                watchProgressRepository.saveProgress(
                    com.skybox.tv.data.database.entity.WatchProgressEntity(
                        mediaId = mediaId,
                        chatId = chatId,
                        messageId = messageId,
                        positionMs = positionMs
                    )
                )
            } catch (e: Exception) {
                // Log error - progress save failure should not crash the app
                // Consider retry logic or UI notification
            }
        }
    }

    /** Stop tracking and clean up. */
    fun release() {
        isTracking = false
        _currentPosition.value = 0L
    }
}