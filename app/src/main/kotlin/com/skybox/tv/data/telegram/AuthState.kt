package com.skybox.tv.data.telegram

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object WaitingForPhoneNumber : AuthState()
    data class WaitingForCode(val phoneNumber: String) : AuthState()
    data class WaitingForPassword(val hint: String) : AuthState()
    object Authorized : AuthState()
    object LoggingOut : AuthState()
    data class Error(val message: String) : AuthState()
}
