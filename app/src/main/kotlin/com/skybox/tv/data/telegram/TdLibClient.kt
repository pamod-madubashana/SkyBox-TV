package com.skybox.tv.data.telegram

class TdLibClient {
    suspend fun initialize() {}
    suspend fun sendPhoneNumber(phoneNumber: String) {}
    suspend fun sendCode(code: String) {}
    suspend fun sendPassword(password: String) {}
    suspend fun logout() {}
    suspend fun getChats(): List<Any> = emptyList()
}
