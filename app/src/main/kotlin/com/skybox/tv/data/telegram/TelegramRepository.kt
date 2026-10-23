package com.skybox.tv.data.telegram

/**
 * Interface defining the contract for Telegram operations.
 * The implementation behind this interface can be replaced (e.g., TDLib vs.
 * a different Telegram client) without affecting the domain layer.
 */
interface TelegramRepository {

    /** Returns the current authorization state flow */
    val authState: kotlinx.coroutines.flow.StateFlow<AuthState>

    /** Authorize with TDLib using the given parameters */
    fun initialize(parameters: TdApi.TdLibParameters)

    /** Set the phone number */
    fun setPhoneNumber(phoneNumber: String)

    /** Set the verification code */
    fun setCode(code: String)

    /** Set the 2FA password */
    fun setPassword(password: String)

    /** Log out from Telegram */
    fun logout()

    /** Fetch a list of chats from Telegram */
    /** Returns a flow of chats; only chats with media will be filtered in the UI */
    val chatFlow: kotlinx.coroutines.flow.StateFlow<List<TdApi.Chat>>

    /** Request a list of chats (starts the TDLib request) */
    fun requestChats()

    /** Look up a media item by its stable media ID */
    fun lookupMedia(mediaId: Long): MediaItem?
}