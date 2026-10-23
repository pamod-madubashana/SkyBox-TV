package com.skybox.tv.domain.usecase

class SaveProgressUseCase {
    suspend operator fun invoke(mediaId: Long, positionMs: Long) {}
}
