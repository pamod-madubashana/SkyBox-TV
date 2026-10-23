package com.skybox.tv.data.catalog

import com.skybox.tv.data.database.SkyBoxDatabase
import com.skybox.tv.data.database.entity.MediaItemEntity
import com.skybox.tv.data.database.entity.WatchProgressEntity
import kotlinx.coroutines.flow.flow

class RoomMediaCatalogRepository private constructor(
    private val database: SkyBoxDatabase
) : MediaCatalogRepository {
    override suspend fun insert(item: MediaItemEntity) {
        database.mediaItemDao().insert(item)
    }

    override suspend fun update(item: MediaItemEntity) {
        database.mediaItemDao().update(item)
    }

    override suspend fun delete(item: MediaItemEntity) {
        database.mediaItemDao().delete(item)
    }

    override suspend fun getById(mediaId: String): MediaItemEntity? {
        return database.mediaItemDao().getById(mediaId)
    }

    override suspend fun getByChatId(chatId: String): java.util.List<MediaItemEntity> {
        return database.mediaItemDao().getByChatId(chatId)
    }

    override suspend fun search(query: String): java.util.List<MediaItemEntity> {
        return database.mediaItemDao().search(query)
    }

    override suspend fun getAllPaged(): Flow<java.util.List<MediaItemEntity>> {
        return database.mediaItemDao().paging()
    }

    companion object {
        fun create(database: SkyBoxDatabase): RoomMediaCatalogRepository {
            return RoomMediaCatalogRepository(database)
        }
    }
}

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