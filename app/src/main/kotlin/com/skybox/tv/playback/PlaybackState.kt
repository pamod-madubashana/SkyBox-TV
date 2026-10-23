/**
 * PlaybackState represents the current state of media playback.
 * This sealed class defines all possible playback states for the UI to observe.
 */
package com.skybox.tv.playback

/**
 * Sealed class representing all possible playback states.
 * UI components can switch on this type to show appropriate controls and messages.
 */
sealed class PlaybackState {

    /** No playback is active or the player has not been initialized. */
    object Idle : PlaybackState()

    /** Media is currently playing. */
    object Playing : PlaybackState()

    /** Media is paused by the user. */
    object Paused : PlaybackState()

    /** Media is buffering - loading more data from the source. */
    object Buffering : PlaybackState()

    /** User is seeking to a different position in the media. */
    object Seeking : PlaybackState()

    /** An error occurred during playback. */
    data class Error(val message: String) : PlaybackState()

    /** Playback has reached the end of the media. */
    object Ended : PlaybackState()
}