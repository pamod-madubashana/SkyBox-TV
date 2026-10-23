/**
 * Represents a Telegram account associated with the user's session.
 * Holds the authorization state and TDLib client reference.
 */
package com.skybox.tv.domain.model

data class TelegramAccount(
    /** Unique identifier for the Telegram account */
    val userId: Long,
    /** Current authorization state */
    val authState: AuthState,
    /** Reference to the TDLib client (nullable during early/init phases) */
    val tdLibClient: TdLibClient? = null,
    /** Timestamp when the account was authorized */
    val authorizedAt: Long = System.currentTimeMillis()
) : Comparable<TelegramAccount> {
    override compareTo(other: TelegramAccount): Int {
        return userId.compareTo(other.userId)
    }
}