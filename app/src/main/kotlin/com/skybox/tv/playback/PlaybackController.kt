package com.skybox.tv.playback

import kotlinx.coroutines.flow.StateFlow

interface PlaybackController {
    val stateFlow: StateFlow<PlaybackState>
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun release()
}
