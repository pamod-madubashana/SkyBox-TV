/**
 * SaveProgressUseCase saves the current playback progress to the repository.
 * This use case is called by the PlayerLifecycleManager when playback is
 * paused, stopped, or sent to background.
 *
 * Pattern: Use case following clean architecture principles.
 * Input: Current playback position and media identifiers
 * Output: Progress persisted to WatchProgressRepository (Phase 3)
 *
 * This use case wraps the existing RoomWatchProgressRepository from Phase 3
 * and provides a domain-level interface for saving watch progress.
 */
package com.skybox.tv.domain.usecase

import com.skybox.tv.data.catalog.WatchProgressRepository
import com.skybox.tv.domain.model.MediaItem

/**
 * Saves the current playback progress for a media item.
 *
 * This use case should be called when:
 * - Playback is paused by the user
 * - Playback is stopped
 * - The app goes to background
 * - The user navigates away from the player screen
 *
 * Typical flow:
 *   1. PlayerLifecycleManager calls SaveProgressUseCase.execute()
 *   2. Use case converts current position to WatchProgressEntity
 *   3. Progress is persisted via RoomWatchProgressRepository
 *   4. Progress can be retrieved later by ResumePlaybackUseCase
 */
class SaveProgressUseCase(
    private val watchProgressRepository: WatchProgressRepository
) {

    /**
     * Save the current playback progress.
     *
     * @param mediaId Unique Telegram media identifier
     * @param chatId Chat identifier where the message was sent
     * @param messageId Message identifier
     * @param positionMs Current playback position in milliseconds
     */
    suspend fun execute(
        mediaId: Long,
        chatId: Long,
        messageId: Long,
        positionMs: Long
    ) {
        // Convert to WatchProgressEntity and persist
        val progressEntity = com.skybox.tv.data.database.entity.WatchProgressEntity(
            mediaId = mediaId,
            chatId = chatId,
            messageId = messageId,
            positionMs = positionMs
        )

        watchProgressRepository.upsert(progressEntity)
    }
}