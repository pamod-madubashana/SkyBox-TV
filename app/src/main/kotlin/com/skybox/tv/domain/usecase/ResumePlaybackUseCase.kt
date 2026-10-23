/**
 * ResumePlaybackUseCase resumes playback of a previously watched media item.
 * This use case retrieves the saved progress from the repository and
 * instructs the playback controller to seek to that position and start playing.
 *
 * Pattern: Use case following clean architecture principles.
 * Input: Media identifiers (mediaId, chatId, messageId)
 * Output: PlaybackController instructions to resume at saved position
 */
package com.skybox.tv.domain.usecase

import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.playback.PlaybackState
import com.skybox.tv.playback.PlaybackController

/**
 * Resumes playback of a media item from a saved position.
 *
 * This use case:
 * 1. Seeks the playback controller to the last saved position
 * 2. Starts playback
 * 3. Updates the playback state to Playing
 *
 * Typical flow:
 *   1. User selects "Continue Watching" from home screen
 *   2. ResumePlaybackUseCase is invoked with the media item
 *   3. Use case seeks and starts playback
 *   4. Playback state flows to UI via PlaybackController.stateFlow
 */
class ResumePlaybackUseCase(
    private val playbackController: PlaybackController
) {

    /**
     * Resume playback of the specified media item from the last saved position.
     *
     * @param mediaId Unique Telegram media identifier
     * @param chatId Chat identifier where the message was sent
     * @param messageId Message identifier
     * @param positionMs Position in milliseconds to resume from (from repository)
     */
    suspend fun resume(
        mediaId: Long,
        chatId: Long,
        messageId: Long,
        positionMs: Long
    ) {
        // Seek to the saved position
        playbackController.seekTo(positionMs)

        // Start playback
        val mediaItem = MediaItem(
            mediaId = mediaId,
            chatId = chatId,
            messageId = messageId,
            fileName = "Telegram media $mediaId",
            mimeType = "video/mp4",
            sizeBytes = 0,
            durationMs = 0
        )

        playbackController.play(mediaItem, com.skybox.tv.playback.DataSource(uri = com.skybox.tv.playback.TelegramMediaDataSource.Uri(mediaId)))

        // State will transition to Playing via the controller's internal logic
    }
}

/**
 * SaveProgressUseCase saves the current playback progress to the repository.
 * This use case is called by the PlayerLifecycleManager when playback is
 * paused, stopped, or sent to background.
 *
 * Pattern: Use case following clean architecture principles.
 * Input: Current playback position and media identifiers
 * Output: Progress persisted to WatchProgressRepository
 */
class SaveProgressUseCase(
    private val progressRepository: com.skybox.tv.data.room.WatchProgressRepository,
    private val progressTracker: com.skybox.tv.playback.ProgressTracker?
) {

    /**
     * Save the current playback progress.
     *
     * This should be called when:
     * - Playback is paused by the user
     * - Playback is stopped
     * - The app goes to background
     * - The user navigates away from the player screen
     *
     * @param mediaId Unique Telegram media identifier
     * @param chatId Chat identifier where the message was sent
     * @param messageId Message identifier
     * @param positionMs Current playback position in milliseconds
     */
    suspend fun saveProgress(
        mediaId: Long,
        chatId: Long,
        messageId: Long,
        positionMs: Long
    ) {
        progressRepository.saveProgress(mediaId, chatId, messageId, positionMs)
    }

    /**
     * Save progress using the ProgressTracker's repository.
     * Convenience method that delegates to the tracker's repository.
     *
     * @param mediaId Unique Telegram media identifier
     * @param chatId Chat identifier where the message was sent
     * @param messageId Message identifier
     * @param positionMs Current playback position in milliseconds
     */
    suspend fun saveProgressViaTracker(
        mediaId: Long,
        chatId: Long,
        messageId: Long,
        positionMs: Long
    ) {
        progressTracker?.let { tracker ->
            tracker.repository.saveProgress(mediaId, chatId, messageId, positionMs)
        } ?: run {
            // Fallback: direct repository save if no tracker
            throw IllegalStateException(
                "No ProgressTracker or WatchProgressRepository available. " +
                    "Initialize ProgressTracker before saving progress."
            )
        }
    }
}