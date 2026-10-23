package com.skybox.tv.domain.usecase

import com.skybox.tv.data.catalog.MediaCatalogRepository
import com.skybox.tv.data.database.entity.MediaItemEntity

class GetMediaItemsUseCase(
    private val repository: MediaCatalogRepository
) {
    suspend fun execute(chatId: String): java.util.List<MediaItemEntity> {
        return repository.getByChatId(chatId)
    }
}