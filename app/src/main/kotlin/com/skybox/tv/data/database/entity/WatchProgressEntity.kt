package com.skybox.tv.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_progress")
data class WatchProgressEntity(
    @PrimaryKey val mediaId: String,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatchedAt: Long,
    val completed: Boolean
)