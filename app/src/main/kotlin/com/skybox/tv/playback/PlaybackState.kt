package com.skybox.tv.playback

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Buffering : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}
