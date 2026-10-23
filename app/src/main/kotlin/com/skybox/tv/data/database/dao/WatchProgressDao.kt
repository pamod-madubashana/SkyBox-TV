package com.skybox.tv.data.database.dao

import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.lifecycle.Flow
import com.skybox.tv.data.database.entity.WatchProgressEntity

@Dao
interface WatchProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(all: WatchProgressEntity)

    @Query("SELECT * FROM watch_progress WHERE mediaId = :mediaId")
    suspend fun getByMediaId(mediaId: String): WatchProgressEntity?

    @Query("SELECT * FROM watch_progress")
    suspend fun getAll(): Flow<java.util.List<WatchProgressEntity>>
}