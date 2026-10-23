package com.skybox.tv.testutil

import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.data.telegram.MediaItem
import com.skybox.tv.data.telegram.TdApi.Chat
import com.skybox.tv.domain.model.TelegramAccount
import com.skybox.tv.data.telegram.TelegramRepository

/**
 * Mock implementation of TelegramRepository for testing purposes.
 * Provides deterministic state transitions and test data.
 */
class FakeTelegramRepository : TelegramRepository {

    // Auth state flow - starts NotConfigured, can be advanced through flow
    private val _authState = androidx.compose.runtime.MutableStateFlow<AuthState>(AuthState.NotConfigured)
    override val authState: androidx.compose.runtime.StateFlow<AuthState> = _authState

    // Chat flow - starts empty
    private val _chatList = androidx.compose.runtime.MutableStateFlow<List<Chat>>(emptyList())
    override val chatFlow: androidx.compose.runtime.StateFlow<List<Chat>> = _chatList

    // User account data
    var userId: Long = 0
    var telegramAccount: TelegramAccount? = null

    // Media items lookup
    private val _mediaItems = mutableMapOf<Long, MediaItem>()

    /** Advance auth state to the next transition. */
    fun advanceAuthState(): AuthState {
        val current = _authState.value
        return when (current) {
            is AuthState.NotConfigured -> {
                _authState.value = AuthState.Initializing
                AuthState.Initializing
            }
            is AuthState.Initializing -> {
                _authState.value = AuthState.WaitingForPhoneNumber
                AuthState.WaitingForPhoneNumber
            }
            is AuthState.WaitingForPhoneNumber -> {
                _authState.value = AuthState.WaitingForCode
                AuthState.WaitingForCode
            }
            is AuthState.WaitingForCode -> {
                _authState.value = AuthState.WaitingForPassword
                AuthState.WaitingForPassword
            }
            is AuthState.WaitingForPassword -> {
                _authState.value = AuthState.Authorized
                AuthState.Authorized
            }
            else -> current
        }
    }

    /** Set auth state to authorized with user ID. */
    fun setAuthorized(userId: Long, account: TelegramAccount?) {
        this.userId = userId
        this.telegramAccount = account
        _authState.value = AuthState.Authorized
    }

    /** Set auth state to error. */
    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    /** Reset auth state to not configured. */
    fun reset() {
        _authState.value = AuthState.NotConfigured
        userId = 0
        telegramAccount = null
        _chatList.value = emptyList()
        _mediaItems.clear()
    }

    /** Add a chat to the chat list. */
    fun addChat(chat: Chat) {
        val current = _chatList.value
        _chatList.value = (current + chat).filter { it.id != 0 }
    }

    /** Set chat list. */
    fun setChatList(chats: List<Chat>) {
        _chatList.value = chats
    }

    /** Add a media item to the lookup map. */
    fun addMediaItem(mediaItem: MediaItem) {
        _mediaItems[mediaItem.mediaId] = mediaItem
    }

    /** Remove a media item from the lookup map. */
    fun removeMediaItem(mediaId: Long) {
        _mediaItems.remove(mediaId)
    }

    // TelegramRepository implementation

    override fun setPhoneNumber(phoneNumber: String) {
        // No-op in fake
    }

    override fun setCode(code: String) {
        // No-op in fake
    }

    override fun setPassword(password: String) {
        // No-op in fake
    }

    override fun logout() {
        reset()
    }

    override fun initialize(parameters: TdApi.TdLibParameters) {
        // No-op in fake
    }

    override val chatFlow: kotlinx.coroutines.flow.StateFlow<List<TdApi.Chat>>
        get() = _chatList.asStateFlow()

    override val authState: kotlinx.coroutines.flow.StateFlow<AuthState>
        get() = _authState.asStateFlow()

    override fun requestChats() {
        // No-op in fake
    }

    override fun lookupMedia(mediaId: Long): MediaItem? {
        return _mediaItems[mediaId]
    }
}