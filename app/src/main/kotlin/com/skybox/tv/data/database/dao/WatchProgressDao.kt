package com.skybox.tv.data.database.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow
import com.skybox.tv.data.database.entity.WatchProgressEntity

@Dao
interface WatchProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchProgressEntity)

    @Query("SELECT * FROM watch_progress WHERE mediaId = :mediaId")
    suspend fun getByMediaId(mediaId: Long): WatchProgressEntity?

    @Query("SELECT * FROM watch_progress")
    fun getAll(): Flow<List<WatchProgressEntity>>
}
