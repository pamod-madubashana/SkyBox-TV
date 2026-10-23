package com.skybox.tv.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.skybox.tv.data.database.entity.MediaItemEntity

@Dao
interface MediaItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(all: MediaItemEntity)

    @Update
    suspend fun update(all: MediaItemEntity)

    @Delete
    suspend fun delete(all: MediaItemEntity)

    @Query("SELECT * FROM media_items WHERE mediaId = :mediaId")
    suspend fun getById(mediaId: String): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE chatId = :chatId")
    suspend fun getByChatId(chatId: String): java.util.List<MediaItemEntity>

    @Query("SELECT * FROM media_items WHERE fileName LIKE :query OR mimeType LIKE :query")
    suspend fun search(query: String): java.util.List<MediaItemEntity>

    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC")
    fun paging(): Flow<java.util.List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE availabilityState = :state")
    suspend fun getByAvailability(state: String): java.util.List<MediaItemEntity>
}