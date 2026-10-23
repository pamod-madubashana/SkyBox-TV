package com.skybox.tv.data.telegram

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TdLibTelegramRepository : TelegramRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.WaitingForPhoneNumber)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun initialize() {}
    override suspend fun sendPhoneNumber(phoneNumber: String) {}
    override suspend fun sendCode(code: String) {}
    override suspend fun sendPassword(password: String) {}
    override suspend fun logout() {}
    override suspend fun getChats(): List<Any> = emptyList()
}
