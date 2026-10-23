package com.skybox.tv.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.skybox.tv.data.database.entity.CacheEntryEntity

@Dao
interface CacheEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CacheEntryEntity)

    @Update
    suspend fun update(entry: CacheEntryEntity)

    @Delete
    suspend fun delete(entry: CacheEntryEntity)

    @Query("SELECT * FROM cache_entries WHERE cacheKey = :cacheKey")
    suspend fun getByKey(cacheKey: String): CacheEntryEntity?

    @Query("SELECT * FROM cache_entries")
    fun getAll(): Flow<List<CacheEntryEntity>>

    @Query("DELETE FROM cache_entries WHERE lastAccessedAt < :threshold")
    suspend fun evictLRU(threshold: Long)
}
