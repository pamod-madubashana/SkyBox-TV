package com.skybox.tv

import com.skybox.tv.playback.PlaybackState

/**
 * Tests for PlaybackState sealed class.
 * Verifies that all playback states are properly defined
 * and can be used in when expressions.
 */
class PlaybackStateTest {

    /** Test that PlaybackState has all expected subclasses. */
    @org.junit.Test
    fun `playback state subclasses are instantiable`() {
        // Idle - singleton object
        assert(PlaybackState.Idle is PlaybackState.Idle)

        // Playing - singleton object
        assert(PlaybackState.Playing is PlaybackState.Playing)

        // Paused - singleton object
        assert(PlaybackState.Paused is PlaybackState.Paused)

        // Buffering - singleton object
        assert(PlaybackState.Buffering is PlaybackState.Buffering)

        // Seeking - singleton object
        assert(PlaybackState.Seeking is PlaybackState.Seeking)

        // Error - data class with message
        val error = PlaybackState.Error("test error")
        assert(error is PlaybackState.Error)
        assert((error as PlaybackState.Error).message == "test error")

        // Ended - singleton object
        assert(PlaybackState.Ended is PlaybackState.Ended)
    }

    /** Test when expression covers all playback states. */
    @org.junit.Test
    fun `when expression covers all playback states`() {
        fun describe(state: PlaybackState): String = when (state) {
            is PlaybackState.Idle -> "idle"
            is PlaybackState.Playing -> "playing"
            is PlaybackState.Paused -> "paused"
            is PlaybackState.Buffering -> "buffering"
            is PlaybackState.Seeking -> "seeking"
            is PlaybackState.Error -> "error"
            is PlaybackState.Ended -> "ended"
        }

        // Test all states can be described
        assert describe(PlaybackState.Idle) == "idle"
        assert describe(PlaybackState.Playing) == "playing"
        assert describe(PlaybackState.Paused) == "paused"
        assert describe(PlaybackState.Buffering) == "buffering"
        assert describe(PlaybackState.Seeking) == "seeking"
        assert describe(PlaybackState.Error("test")) == "error"
        assert describe(PlaybackState.Ended) == "ended"
    }
}