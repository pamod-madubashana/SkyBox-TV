package com.skybox.tv.domain.usecase

class SaveWatchProgressUseCase {
    suspend operator fun invoke(mediaId: Long, positionMs: Long) {}
}
