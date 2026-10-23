package com.skybox.tv.data.catalog

class RoomWatchProgressRepository : WatchProgressRepository {
    override suspend fun upsert(mediaId: Long, positionMs: Long) {}
    override suspend fun get(mediaId: Long): Long? = null
    override suspend fun getAll(): List<Any> = emptyList()
}
