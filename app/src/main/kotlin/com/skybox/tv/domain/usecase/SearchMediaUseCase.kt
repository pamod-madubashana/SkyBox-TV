package com.skybox.tv.domain.usecase

import com.skybox.tv.data.catalog.MediaCatalogRepository

class SearchMediaUseCase(
    private val repository: MediaCatalogRepository
) {
    suspend fun execute(query: String): java.util.List<MediaItemEntity> {
        return repository.search(query)
    }
}