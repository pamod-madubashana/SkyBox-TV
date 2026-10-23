package com.skybox.tv.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExoPlayerPlaybackController : PlaybackController {
    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val stateFlow: StateFlow<PlaybackState> = _state.asStateFlow()

    override fun play() {}
    override fun pause() {}
    override fun stop() {}
    override fun seekTo(positionMs: Long) {}
    override fun release() {}
}
