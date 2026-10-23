package com.skybox.tv.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries", indices = [Index("cacheKey"), Index("lastAccessedAt")])
data class CacheEntryEntity(
    @PrimaryKey val cacheKey: String,
    val localPath: String,
    val sizeBytes: Long,
    val lastAccessedAt: Long,
    val state: String
)