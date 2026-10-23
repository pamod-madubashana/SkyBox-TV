package com.skybox.tv.data.telegram

import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeCancellableCoroutine
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Wraps org.drinkless.tdlib.Client and handles the TDLib lifecycle.
 * Uses a coroutine-based receive loop to dispatch updates.
 */
class TdLibClient(
    private val onAuthorizationStateChanged: (AuthState) -> Unit,
    private val onChatReceived: (TdApi.Chat) -> Unit,
    private val onMessageReceived: (TdApi.Message) -> Unit
) {
    private val client = Client()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Default)

    init {
        // Set logging level (disabled for production - never log phone codes/passwords)
        client.setLogVerbosityLevel(0)

        // Start the receive loop in a coroutine
        scope.launch {
            receiveLoop()
        }
    }

    /** Send a TDLib request */
    suspend fun send(request: TdApi.Request): TdApi.Response {
        return client.send(request)
    }

    /** Receive updates from TDLib (non-blocking poll) */
    private suspend fun receive(timeoutMillis: Int = 500): TdApi.Update? {
        return client.receive(timeoutMillis)
    }

    /** Set TDLib parameters (called during initialization) */
    suspend fun setParameters(params: TdApi.TdLibParameters) {
        client.setParameters(params)
    }

    /** Get the current authorization state */
    suspend fun getAuthorizationState(): AuthState {
        val state = client.getAuthorizationState()
        return when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> AuthState.Initializing
            is TdApi.AuthorizationStateWaitPhoneNumber -> AuthState.WaitingForPhoneNumber
            is TdApi.AuthorizationStateWaitCode -> AuthState.WaitingForCode
            is TdApi.AuthorizationStateWaitPassword -> AuthState.WaitingForPassword
            is TdApi.AuthorizationStateReady -> AuthState.Authorized
            is TdApi.AuthorizationStateError -> AuthState.Error(
                (state as TdApi.AuthorizationStateError).message
            )
            is TdApi.AuthorizationStateClosed -> AuthState.LoggingOut
            else -> AuthState.Error("Unknown authorization state: ${state.javaClass.simpleName}")
        }
    }

    /** Begin the authorization flow by setting TDLib parameters */
    suspend fun initialize(parameters: TdApi.TdLibParameters) {
        setParameters(parameters)
        // Request authorization state will be received via receive()
    }

    /** Send the phone number to TDLib */
    suspend fun sendPhoneNumber(phoneNumber: String) {
        client.send(TdApi.setAuthenticationPhoneNumber(phoneNumber))
    }

    /** Send the verification code to TDLib */
    suspend fun sendCode(code: String) {
        client.send(TdApi.setAuthenticationCode(code))
    }

    /** Send the password for 2FA */
    suspend fun sendPassword(password: String) {
        client.send(TdApi.setAuthenticationPassword(password))
    }

    /** Log out and close the TDLib client */
    fun logout() {
        scope.cancel()
        client.close()
        onAuthorizationStateChanged(AuthState.LoggingOut)
    }

    /** Start the receive loop that dispatches updates to callbacks */
    private suspend fun receiveLoop() {
        while (!job.isCancelled) {
            val update = receive(500)
            if (update != null) {
                when (update) {
                    is TdApi.UpdateAuthorizationState -> {
                        val newState = getAuthorizationState()
                        onAuthorizationStateChanged(newState)
                    }
                    is TdApi.UpdateNewChat -> onChatReceived(update.chat)
                    is TdApi.UpdateNewMessage -> onMessageReceived(update.message)
                    else -> {}
                }
            }
        }
    }

    /** Dispose of the coroutine scope */
    fun dispose() {
        scope.cancel()
    }
}