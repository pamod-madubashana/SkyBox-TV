package com.skybox.tv.data.catalog

import com.skybox.tv.data.database.SkyBoxDatabase
import com.skybox.tv.data.database.entity.WatchProgressEntity
import kotlinx.coroutines.flow.Flow

class RoomWatchProgressRepository private constructor(
    private val database: SkyBoxDatabase
) : WatchProgressRepository {
    override suspend fun upsert(progress: WatchProgressEntity) {
        database.watchProgressDao().upsert(progress)
    }

    override suspend fun getByMediaId(mediaId: String): com.skybox.tv.data.database.entity.WatchProgressEntity? {
        return database.watchProgressDao().getByMediaId(mediaId)
    }

    override suspend fun getAll(): Flow<java.util.List<WatchProgressEntity>> {
        return database.watchProgressDao().getAll()
    }

    companion object {
        fun create(database: SkyBoxDatabase): RoomWatchProgressRepository {
            return RoomWatchProgressRepository(database)
        }
    }
}