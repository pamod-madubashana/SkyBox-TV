package com.skybox.tv

import com.skybox.tv.data.telegram.AuthState

/**
 * Tests for AuthState sealed class.
 * Verifies that all authorization states are properly defined
 * and can be used in when expressions.
 */
class AuthStateTest {

    /** Test that AuthState has all expected subclasses. */
    @org.junit.Test
    fun `auth state subclasses are instantiable`() {
        // NotConfigured - no-arg data object
        val state1 = AuthState.NotConfigured
        assert(state1 is AuthState.NotConfigured)

        // Initializing - no-arg data object
        val state2 = AuthState.Initializing
        assert(state2 is AuthState.Initializing)

        // WaitingForPhoneNumber - no-arg data object
        val state3 = AuthState.WaitingForPhoneNumber
        assert(state3 is AuthState.WaitingForPhoneNumber)

        // WaitingForCode - no-arg data object
        val state4 = AuthState.WaitingForCode
        assert(state4 is AuthState.WaitingForCode)

        // WaitingForPassword - no-arg data object
        val state5 = AuthState.WaitingForPassword
        assert(state5 is AuthState.WaitingForPassword)

        // Authorized - no-arg data object
        val state6 = AuthState.Authorized
        assert(state6 is AuthState.Authorized)

        // LoggingOut - no-arg data object
        val state7 = AuthState.LoggingOut
        assert(state7 is AuthState.LoggingOut)

        // Error - data object with message
        val state8 = AuthState.Error("test error")
        assert(state8 is AuthState.Error)
        assert((state8 as AuthState.Error).message == "test error")
    }

    /** Test when expression covers all auth states. */
    @org.junit.Test
    fun `when expression covers all auth states`() {
        fun describe(state: AuthState): String = when (state) {
            is AuthState.NotConfigured -> "not configured"
            is AuthState.Initializing -> "initializing"
            is AuthState.WaitingForPhoneNumber -> "waiting for phone number"
            is AuthState.WaitingForCode -> "waiting for code"
            is AuthState.WaitingForPassword -> "waiting for password"
            is AuthState.Authorized -> "authorized"
            is AuthState.LoggingOut -> "logging out"
            is AuthState.Error -> "error"
        }

        // Test all states can be described
        assert describe(AuthState.NotConfigured) == "not configured"
        assert describe(AuthState.Initializing) == "initializing"
        assert describe(AuthState.WaitingForPhoneNumber) == "waiting for phone number"
        assert describe(AuthState.WaitingForCode) == "waiting for code"
        assert describe(AuthState.WaitingForPassword) == "waiting for password"
        assert describe(AuthState.Authorized) == "authorized"
        assert describe(AuthState.LoggingOut) == "logging out"
        assert describe(AuthState.Error("test")) == "error"
    }
}