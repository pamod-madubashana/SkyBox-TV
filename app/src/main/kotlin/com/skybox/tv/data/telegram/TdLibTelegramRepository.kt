package com.skybox.tv.data.telegram

import org.drinkless.tdlib.TdApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

/** Implementation of TelegramRepository using org.drinkless.tdlib.Client. */
class TdLibTelegramRepository(
    private val tdLibClient: TdLibClient
) : TelegramRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotConfigured)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _chatList = MutableStateFlow<List<TdApi.Chat>>(emptyList())
    val chatFlow: StateFlow<List<TdApi.Chat>> = _chatList.asStateFlow()

    private var currentPhoneNumber = ""
    private var currentAuthorizationCode = ""

    init {
        // Observe TDLib client authorization state changes
        tdLibClient.onAuthorizationStateChanged { state ->
            _authState.value = state
            // Propagate chat list when authorized
            if (state == AuthState.Authorized) {
                requestChatsInternal()
            }
        }

        // Observe chat updates from TDLib
        tdLibClient.onChatReceived { chat ->
            // Update chat list - simple approach: replace or add
            val current = _chatList.value
            if (current.all { it.id != chat.id }) {
                _chatList.value = listOf(chat) + current
            }
        }
    }

    override fun initialize(parameters: TdApi.TdLibParameters) {
        tdLibClient.initialize(parameters)
    }

    override fun setPhoneNumber(phoneNumber: String) {
        currentPhoneNumber = phoneNumber
        tdLibClient.sendPhoneNumber(phoneNumber)
    }

    override fun setCode(code: String) {
        currentAuthorizationCode = code
        tdLibClient.sendCode(code)
    }

    override fun setPassword(password: String) {
        tdLibClient.sendPassword(password)
    }

    override fun logout() {
        tdLibClient.logout()
        _authState.value = AuthState.NotConfigured
        _chatList.value = emptyList()
    }

    override fun requestChats() {
        requestChatsInternal()
    }

    private fun requestChatsInternal() {
        val request = TdApi.getChats(limit = 50)
        tdLibClient.send(request)
        // The chat updates will arrive via the onChatReceived callback
        // We need to wait for them - for now, trigger a receive cycle
        // In a full implementation, this would use proper suspension/callbacks
    }

    override fun lookupMedia(mediaId: Long): MediaItem? {
        // TODO: Implement media lookup from TDLib responses
        // For now, return null as this requires full message parsing
        return null
    }
}