package com.skybox.tv.data.telegram

/**
 * Sealed class representing the TDLib authorization state.
 * Each state corresponds to a step in the Telegram authorization flow.
 */
sealed class AuthState {

    /** TDLib has not been configured yet. No client initialized. */
    data object NotConfigured : AuthState()

    /** TDLib client is being initialized (parameters set, auth started). */
    data object Initializing : AuthState()

    /** Client is waiting for the user to provide their phone number. */
    data object WaitingForPhoneNumber : AuthState()

    /** Client is waiting for the user to provide the verification code. */
    data object WaitingForCode : AuthState()

    /** Client is waiting for the user to provide their password (for 2FA). */
    data object WaitingForPassword : AuthState()

    /** Client is successfully authorized. */
    data object Authorized : AuthState()

    /** Client is in the process of logging out. */
    data object LoggingOut : AuthState()

    /** An error occurred during authorization. */
    data object Error(val message: String) : AuthState()
}