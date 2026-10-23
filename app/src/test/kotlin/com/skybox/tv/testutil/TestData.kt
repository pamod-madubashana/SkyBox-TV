package com.skybox.tv.testutil

import com.skybox.tv.data.database.entity.MediaItemEntity
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.data.telegram.MediaItem
import com.skybox.tv.data.telegram.TdApi.Chat
import com.skybox.tv.domain.model.TelegramAccount
import java.util.concurrent.ConcurrentHashMap

/**
 * Test data factories for creating test entities, models, and states.
 * Provides convenient methods to generate consistent test data.
 */
object TestData {

    /** Create a minimal MediaItemEntity for testing. */
    fun mediaItemEntity(
        mediaId: String = "test_media",
        accountId: String = "test_account",
        chatId: String = "test_chat",
        messageId: String = "test_message",
        fileId: String = "test_file",
        fileName: String = "test.mp4",
        mimeType: String = "video/mp4",
        sizeBytes: Long = 1024L,
        durationMs: Long = 300000L,
        thumbnailReference: String = "thumb_test",
        dateAdded: Long = System.currentTimeMillis(),
        availabilityState: String = "available"
    ): MediaItemEntity = MediaItemEntity(
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

    /** Create a test MediaItem. */
    fun mediaItem(
        mediaId: Long = 123L,
        fileId: String = "file_456",
        fileName: String = "test_video.mp4",
        mimeType: String = "video/mp4",
        chatId: String = "chat_789",
        messageId: String = "msg_101",
        title: String? = "Test Video",
        durationMs: Long = 300000L,
        sizeBytes: Long = 1024L
    ): MediaItem = MediaItem(
        mediaId = mediaId,
        fileId = fileId,
        fileName = fileName,
        mimeType = mimeType,
        chatId = chatId,
        messageId = messageId,
        title = title,
        durationMs = durationMs,
        sizeBytes = sizeBytes
    )

    /** Create a test Chat. */
    fun chat(
        chatId: Long = 1L,
        title: String = "Test Chat",
        type: TdApi.ChatType = TdApi.ChatType.Private
    ): Chat = Chat(
        id = chatId,
        firstName = null,
        lastName = null,
        username = "test_chat",
        photo = null,
        date = 0,
        type = type,
        status = TdApi.ChatStatus.ChatMember,
        notificationSettings = TdApi.ChatNotificationSettings(),
        location = null,
        lastMessage = null,
        unsubscribed = false,
        archived = false,
        pinnedMessageId = 0,
        lastReadMessageId = TdApi.MessageId(),
        unreadCount = 0,
        isSecret = false,
        slowMode = false,
        isChannel = false,
        isForum = false,
        isEmpty = false,
        isRecent = false,
        isFavorite = false,
        isUser = false,
        restrictContent = false,
        canBeEdited = false,
        canBeDeleted = false,
        canChangeInfo = false,
        canPostMessages = false,
        canEditMessages = false,
        canRestrictMembers = false,
        canPromoteMembers = false,
        canViewStats = false,
        canInviteUsers = false,
        canChangeSettings = false,
        canDeleteMessages = false,
        canPostStories = false,
        canEditStories = false,
        canDeleteStories = false,
        canInviteToGroup = false,
        canRemoveFromGroup = false
    )

    /** Create a test TelegramAccount. */
    fun telegramAccount(
        userId: Long = 12345L,
        phoneNumber: String = "+1234567890",
        isConnected: Boolean = true
    ): TelegramAccount = TelegramAccount(
        userId = userId,
        phoneNumber = phoneNumber,
        isConnected = isConnected
    )

    /** Create an AuthState.NotConfigured. */
    fun notConfiguredAuthState(): AuthState = AuthState.NotConfigured

    /** Create an AuthState.Initializing. */
    fun initializingAuthState(): AuthState = AuthState.Initializing

    /** Create an AuthState.WaitingForPhoneNumber. */
    fun waitingForPhoneNumberAuthState(): AuthState = AuthState.WaitingForPhoneNumber

    /** Create an AuthState.WaitingForCode. */
    fun waitingForCodeAuthState(): AuthState = AuthState.WaitingForCode

    /** Create an AuthState.WaitingForPassword. */
    fun waitingForPasswordAuthState(): AuthState = AuthState.WaitingForPassword

    /** Create an AuthState.Authorized. */
    fun authorizedAuthState(): AuthState = AuthState.Authorized

    /** Create an AuthState.Error. */
    fun errorAuthState(message: String = "test error"): AuthState = AuthState.Error(message)

    /** Create a list of test chats. */
    fun chatsList(count: Int = 5): List<Chat> = (1..count).map { i ->
        chat(chatId = Long.toLong(i), title = "Chat $i")
    }

    /** Create a list of test media items. */
    fun mediaItemsList(count: Int = 3): List<MediaItem> = (1..count).map { i ->
        mediaItem(
            mediaId = Long.toLong(i),
            fileName = "video_$i.mp4",
            title = "Video $i"
        )
    }

    /** Create a map of media items. */
    fun mediaItemMap(items: MediaItem...): Map<Long, MediaItem> {
        return ConcurrentHashMap(items.associate { it.mediaId to it })
    }
}

/** Data model for test media items. */
data class MediaItem(
    val mediaId: Long,
    val fileId: String,
    val fileName: String,
    val mimeType: String,
    val chatId: String,
    val messageId: String,
    val title: String?,
    val durationMs: Long,
    val sizeBytes: Long
)