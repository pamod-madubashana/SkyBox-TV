package com.skybox.tv.presentation.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PlaybackSessionState {
    object Idle : PlaybackSessionState()
    object Playing : PlaybackSessionState()
    object Paused : PlaybackSessionState()
    object Buffering : PlaybackSessionState()
    data class Error(val message: String) : PlaybackSessionState()
}

class PlaybackSession {
    private val _state = MutableStateFlow<PlaybackSessionState>(PlaybackSessionState.Idle)
    val state: StateFlow<PlaybackSessionState> = _state.asStateFlow()

    fun play() {}
    fun pause() {}
    fun stop() {}
    fun seekTo(positionMs: Long) {}
    fun release() {}
}
