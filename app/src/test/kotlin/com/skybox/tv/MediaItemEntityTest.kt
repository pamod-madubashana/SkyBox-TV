package com.skybox.tv

import com.skybox.tv.data.database.entity.MediaItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for MediaItemEntity Room entity.
 * Verifies that the entity can be created with valid data
 * and that all fields are properly stored.
 */
class MediaItemEntityTest {

    @Test
    fun `media item entity can be created with valid data`() {
        val mediaId = "media_123"
        val accountId = "account_456"
        val chatId = "chat_789"
        val messageId = "msg_101"
        val fileId = "file_202"
        val fileName = "test.mp4"
        val mimeType = "video/mp4"
        val sizeBytes = 1024L
        val durationMs = 300000L
        val thumbnailReference = "thumb_303"
        val dateAdded = System.currentTimeMillis()
        val availabilityState = "available"

        val entity = MediaItemEntity(
            mediaId = mediaId,
            accountId = accountId,
            chatId = chatId,
            messageId = messageId,
            fileId = fileId,
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            durationMs = durationMs,
            thumbnailReference = thumbnailReference,
            dateAdded = dateAdded,
            availabilityState = availabilityState
        )

        assertEquals("mediaId not matched", mediaId, entity.mediaId)
        assertEquals("accountId not matched", accountId, entity.accountId)
        assertEquals("chatId not matched", chatId, entity.chatId)
        assertEquals("messageId not matched", messageId, entity.messageId)
        assertEquals("fileId not matched", fileId, entity.fileId)
        assertEquals("fileName not matched", fileName, entity.fileName)
        assertEquals("mimeType not matched", mimeType, entity.mimeType)
        assertEquals("sizeBytes not matched", sizeBytes, entity.sizeBytes)
        assertEquals("durationMs not matched", durationMs, entity.durationMs)
        assertEquals("thumbnailReference not matched", thumbnailReference, entity.thumbnailReference)
        assertEquals("dateAdded not matched", dateAdded, entity.dateAdded)
        assertEquals("availabilityState not matched", availabilityState, entity.availabilityState)
    }

    @Test
    fun `media item entity defaults work`() {
        // Test that entity can be created with just the @PrimaryKey
        val entity = MediaItemEntity(mediaId = "minimal")
        assertEquals("mediaId not matched", "minimal", entity.mediaId)
    }
}