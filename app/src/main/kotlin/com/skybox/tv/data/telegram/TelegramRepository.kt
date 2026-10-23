package com.skybox.tv.data.telegram

import kotlinx.coroutines.flow.StateFlow

interface TelegramRepository {
    val authState: StateFlow<AuthState>
    suspend fun initialize()
    suspend fun sendPhoneNumber(phoneNumber: String)
    suspend fun sendCode(code: String)
    suspend fun sendPassword(password: String)
    suspend fun logout()
    suspend fun getChats(): List<Any>
}
