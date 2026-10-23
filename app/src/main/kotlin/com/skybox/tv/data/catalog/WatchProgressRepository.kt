package com.skybox.tv.data.catalog

import com.skybox.tv.data.database.SkyBoxDatabase
import com.skybox.tv.data.database.entity.MediaItemEntity
import kotlinx.coroutines.flow.Flow

interface WatchProgressRepository {
    suspend fun upsert(progress: WatchProgressEntity)
    suspend fun getByMediaId(mediaId: String): com.skybox.tv.data.database.entity.WatchProgressEntity?
    suspend fun getAll(): Flow<java.util.List<WatchProgressEntity>>
}