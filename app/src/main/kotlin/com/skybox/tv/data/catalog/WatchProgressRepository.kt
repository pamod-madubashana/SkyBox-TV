package com.skybox.tv.data.catalog

interface WatchProgressRepository {
    suspend fun upsert(mediaId: Long, positionMs: Long)
    suspend fun get(mediaId: Long): Long?
    suspend fun getAll(): List<Any>
}
