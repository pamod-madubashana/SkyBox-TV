package com.skybox.tv.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skybox.tv.data.database.entity.MediaSourceEntity

@Dao
interface MediaSourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(all: MediaSourceEntity)

    @Update
    suspend fun update(all: MediaSourceEntity)

    @Query("SELECT * FROM media_sources WHERE chatId = :chatId")
    suspend fun getByChatId(chatId: String): java.util.List<MediaSourceEntity>

    @Query("SELECT * FROM media_sources")
    suspend fun getAll(): java.util.List<MediaSourceEntity>
}