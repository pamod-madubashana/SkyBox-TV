package com.skybox.tv.data.catalog

import com.skybox.tv.data.database.entity.MediaItemEntity

interface MediaCatalogRepository {
    suspend fun insert(item: MediaItemEntity)
    suspend fun update(item: MediaItemEntity)
    suspend fun delete(item: MediaItemEntity)
    suspend fun getById(mediaId: String): MediaItemEntity?
    suspend fun getByChatId(chatId: String): java.util.List<MediaItemEntity>
    suspend fun search(query: String): java.util.List<MediaItemEntity>
    suspend fun getAllPaged(): androidx.lifecycle.flow.Flow<java.util.List<MediaItemEntity>>
}