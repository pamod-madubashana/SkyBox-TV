package com.skybox.tv.data.database.dao

import androidx.room.CursorResult
import androidx.room.Delete
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.lifecycle.Flow
import com.skybox.tv.data.database.entity.CacheEntryEntity

@Dao
interface CacheEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(all: CacheEntryEntity)

    @Update
    suspend fun update(all: CacheEntryEntity)

    @Delete
    suspend fun delete(all: CacheEntryEntity)

    @Query("SELECT * FROM cache_entries WHERE cacheKey = :cacheKey")
    suspend fun getByKey(cacheKey: String): CacheEntryEntity?

    @Query("SELECT * FROM cache_entries")
    suspend fun getAll(): Flow<java.util.List<CacheEntryEntity>>

    @Query("DELETE FROM cache_entries WHERE lastAccessedAt < :threshold")
    suspend fun evictLRU(threshold: Long)
}