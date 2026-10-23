package com.skybox.tv.domain.usecase

import com.skybox.tv.data.catalog.WatchProgressRepository

class GetWatchProgressUseCase(
    private val repository: WatchProgressRepository
) {
    suspend fun execute(mediaId: String): com.skybox.tv.data.database.entity.WatchProgressEntity? {
        return repository.getByMediaId(mediaId)
    }
}