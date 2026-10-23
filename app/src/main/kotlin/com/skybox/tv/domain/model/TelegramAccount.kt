package com.skybox.tv.domain.model

data class TelegramAccount(
    val userId: Long = 0L,
    val phoneNumber: String = "",
    val firstName: String = "",
    val lastName: String = ""
)
