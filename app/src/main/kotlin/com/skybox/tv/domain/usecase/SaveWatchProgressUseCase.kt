package com.skybox.tv.domain.usecase

import com.skybox.tv.data.catalog.WatchProgressRepository
import com.skybox.tv.data.database.entity.WatchProgressEntity

class SaveWatchProgressUseCase(
    private val repository: WatchProgressRepository
) {
    suspend fun execute(progress: WatchProgressEntity) {
        repository.upsert(progress)
    }
}

class GetWatchProgressUseCase(
    private val repository: WatchProgressRepository
) {
    suspend fun execute(mediaId: String): com.skybox.tv.data.database.entity.WatchProgressEntity? {
        return repository.getByMediaId(mediaId)
    }
}