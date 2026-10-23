package com.skybox.tv.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "media_items", indices = [Index("chatId"), Index("mimeType"), Index("availabilityState")])
data class MediaItemEntity(
    @PrimaryKey val mediaId: String,
    val accountId: String,
    val chatId: String,
    val messageId: String,
    val fileId: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val thumbnailReference: String,
    val dateAdded: Long,
    val availabilityState: String
)