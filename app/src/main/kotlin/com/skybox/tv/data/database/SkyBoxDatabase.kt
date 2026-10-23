package com.skybox.tv.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.skybox.tv.data.database.entity.CacheEntryEntity
import com.skybox.tv.data.database.entity.MediaItemEntity
import com.skybox.tv.data.database.entity.MediaSourceEntity
import com.skybox.tv.data.database.entity.WatchProgressEntity

@Database(entities = [
    MediaItemEntity::class,
    WatchProgressEntity::class,
    MediaSourceEntity::class,
    CacheEntryEntity::class
], version = 1, exportSchema = false)
abstract class SkyBoxDatabase : RoomDatabase() {

    abstract fun mediaItemDao(): com.skybox.tv.data.database.dao.MediaItemDao
    abstract fun watchProgressDao(): com.skybox.tv.data.database.dao.WatchProgressDao
    abstract fun mediaSourceDao(): com.skybox.tv.data.database.dao.MediaSourceDao
    abstract fun cacheEntryDao(): com.skybox.tv.data.database.dao.CacheEntryDao

    companion object {
        @Volatile
        private var INSTANCE: SkyBoxDatabase? = null

        fun getInstance(context: android.content.Context): SkyBoxDatabase {
            // Use synchronized to handle concurrent access
            return INSTANCE ?: synchronized(this) {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    SkyBoxDatabase::class.java,
                    "sky_box_db"
                ).build()
                INSTANCE = database
                database
            }
        }
    }
}