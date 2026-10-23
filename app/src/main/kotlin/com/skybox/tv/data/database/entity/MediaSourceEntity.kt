package com.skybox.tv.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "media_sources", indices = [Index("chatId"), Index("accountId"), Index("isEnabled")])
data class MediaSourceEntity(
    @PrimaryKey val sourceId: String,
    val accountId: String,
    val chatId: String,
    val displayName: String,
    val isEnabled: Boolean,
    val lastIndexedMessageId: String
)