/**
 * TrackSelector handles audio track and subtitle selection for media playback.
 * This class works with ExoPlayer's TrackSelector to allow users to choose
 * different audio tracks and subtitle languages during playback.
 *
 * Pattern: Wrapper around ExoPlayer's TrackSelector with additional
 * convenience methods for common subtitle/audio operations.
 *
 * Responsibilities:
 * - Provide current audio track information
 * - Provide current subtitle track information
 * - Select audio track by index or language
 * - Select subtitle track by index or language
 * - List available audio and subtitle tracks
 * - Handle track selection changes
 */
package com.skybox.tv.playback

import androidx.media3.common.Player
import androidx.media3.common.audio.AudioAttributes
import androidx.media3.common.video.VideoFormat
import androidx.media3.exoplayer.trackselection.*
import com.skybox.tv.domain.model.MediaItem

/**
 * Selection representing an audio track option.
 */
data class AudioTrackOption(
    /** Index of the audio track in the player */
    val trackIndex: Int,
    /** Language code (e.g., "en", "es", "fr") */
    val languageCode: String?,
    /** Human-readable track name */
    val name: String,
    /** Whether this track is enabled by default */
    val isDefault: Boolean = false
)

/**
 * Selection representing a subtitle track option.
 */
data class SubtitleTrackOption(
    /** Index of the subtitle track in the player */
    val trackIndex: Int,
    /** Language code (e.g., "en", "es", "fr") */
    val languageCode: String?,
    /** Human-readable track name (often from SRT/WebVTT) */
    val name: String,
    /** Whether this track is enabled by default */
    val isDefault: Boolean = false
    /** Whether this is a forced subtitle track */
    val isForced: Boolean = false
)

/**
 * Result of a track selection operation.
 */
sealed class TrackSelectionResult {

    /** Track was selected successfully. */
    data class Success(
        /** The selected track option */
        val trackOption: SubtitleTrackOption,
        /** Whether the track was newly selected */
        val wasNewlySelected: Boolean
    ) : TrackSelectionResult()

    /** Track selection failed (track not available). */
    data class Failure(
        /** Description of the failure */
        val reason: String
    ) : TrackSelectionResult()

    /** No tracks available for the current media. */
    object NoTracksAvailable : TrackSelectionResult()
}

/**
 * Interface for track selection callbacks.
 */
interface TrackSelectionCallback {

    /** Called when an audio track is selected. */
    fun onAudioTrackSelected(trackOption: AudioTrackOption)

    /** Called when a subtitle track is selected. */
    fun onSubtitleTrackSelected(trackOption: SubtitleTrackOption)

    /** Called when track selection fails. */
    fun onTrackSelectionFailed(reason: String)
}

/**
 * Manager for audio and subtitle track selection during playback.
 *
 * This class abstracts the ExoPlayer TrackSelector operations and provides
 * a simpler interface for UI components to select audio and subtitle tracks.
 *
 * Usage:
 *   1. Create with an ExoPlayer instance
 *   2. Query available tracks using available* methods
 *   3. Select tracks using selectAudioTrack() or selectSubtitleTrack()
 *   4. Observe changes via callback
 */
class TrackSelectorManager(
    private val player: Player
) {

    /** Current audio track option, or null if none selected. */
    private var currentAudioTrack: AudioTrackOption? = null

    /** Current subtitle track option, or null if none selected. */
    private var currentSubtitleTrack: SubtitleTrackOption? = null

    /**
     * Get available audio track options for the current media.
     *
     * @return List of AudioTrackOption available, or empty list if none
     */
    @UnstableApi
    fun getAvailableAudioTracks(): List<AudioTrackOption> {
        val result = mutableListOf<AudioTrackOption>()

        // ExoPlayer provides audio tracks via the track selector
        // We need to query the player's audio capabilities
        val audioTrackCount = player.audioTrackCount
        if (audioTrackCount <= 1) {
            // No alternative audio tracks, return empty or default
            return result
        }

        // Get audio attributes for each track
        for (i in 0 until audioTrackCount) {
            try {
                val attributes = player.getAudioAttributes(i)
                val language = attributes.language
                val name = if (language != null) language : "Track $i"
                result.add(AudioTrackOption(
                    trackIndex = i,
                    languageCode = language,
                    name = name
                ))
            } catch (e: Exception) {
                // Skip tracks that can't be queried
            }
        }

        return result
    }

    /**
     * Get available subtitle track options for the current media.
     *
     * @return List of SubtitleTrackOption available, or empty list if none
     */
    @UnstableApi
    fun getAvailableSubtitleTracks(): List<SubtitleTrackOption> {
        val result = mutableListOf<SubtitleTrackOption>()

        // ExoPlayer provides subtitle tracks via the track selector
        val subtitleTrackCount = player.subtitleTrackCount
        if (subtitleTrackCount <= 1) {
            // No alternative subtitle tracks
            return result
        }

        // Get text tracks/captions from the player
        // Note: ExoPlayer's API for this may vary by version
        for (i in 0 until subtitleTrackCount) {
            try {
                val trackInfo = player.getTextSelection(i)
                val language = trackInfo.language
                val name = if (language != null) language : "Subtitle $i"
                result.add(SubtitleTrackOption(
                    trackIndex = i,
                    languageCode = language,
                    name = name
                ))
            } catch (e: Exception) {
                // Skip tracks that can't be queried
            }
        }

        return result
    }

    /**
     * Select an audio track by its index.
     *
     * @param trackIndex Index of the audio track to select
     * @return TrackSelectionResult indicating success or failure
     */
    @UnstableApi
    fun selectAudioTrack(trackIndex: Int): TrackSelectionResult {
        try {
            player.setSelectedAudioTrackIndex(trackIndex)

            // Update internal state
            val option = AudioTrackOption(
                trackIndex = trackIndex,
                languageCode = null, // Would need to query again
                name = "Track $trackIndex"
            )
            currentAudioTrack = option

            return TrackSelectionResult.Success(trackOption = option, wasNewlySelected = true)
        } catch (e: Exception) {
            return TrackSelectionResult.Failure(
                reason = "Failed to select audio track $trackIndex: ${e.message}"
            )
        }
    }

    /**
     * Select a subtitle track by its index.
     *
     * @param trackIndex Index of the subtitle track to select
     * @return TrackSelectionResult indicating success or failure
     */
    @UnstableApi
    fun selectSubtitleTrack(trackIndex: Int): TrackSelectionResult {
        try {
            player.setSelectedTextTrackIndex(trackIndex)

            // Update internal state
            val option = SubtitleTrackOption(
                trackIndex = trackIndex,
                languageCode = null, // Would need to query again
                name = "Subtitle $trackIndex"
            )
            currentSubtitleTrack = option

            return TrackSelectionResult.Success(trackOption = option, wasNewlySelected = true)
        } catch (e: Exception) {
            return TrackSelectionResult.Failure(
                reason = "Failed to select subtitle track $trackIndex: ${e.message}"
            )
        }
    }

    /**
     * Select an audio track by language code.
     *
     * @param languageCode Language code (e.g., "en", "es", "fr") or null for default
     * @return TrackSelectionResult indicating success or failure
     */
    @UnstableApi
    fun selectAudioTrackByLanguage(languageCode: String?): TrackSelectionResult {
        val availableTracks = getAvailableAudioTracks()
        val selectedTrack = availableTracks.find { it.languageCode == languageCode }

        return if (selectedTrack != null) {
            selectAudioTrack(selectedTrack.trackIndex)
        } else {
            TrackSelectionResult.Failure(
                reason = "No audio track found with language code: $languageCode"
            )
        }
    }

    /**
     * Select a subtitle track by language code.
     *
     * @param languageCode Language code (e.g., "en", "es", "fr") or null for default
     * @return TrackSelectionResult indicating success or failure
     */
    @UnstableApi
    fun selectSubtitleTrackByLanguage(languageCode: String?): TrackSelectionResult {
        val availableTracks = getAvailableSubtitleTracks()
        val selectedTrack = availableTracks.find { it.languageCode == languageCode }

        return if (selectedTrack != null) {
            selectSubtitleTrack(selectedTrack.trackIndex)
        } else {
            TrackSelectionResult.Failure(
                reason = "No subtitle track found with language code: $languageCode"
            )
        }
    }

    /**
     * Get the currently selected audio track option.
     *
     * @return Current AudioTrackOption, or null if no audio tracks available
     */
    fun getCurrentAudioTrack(): AudioTrackOption? = currentAudioTrack

    /**
     * Get the currently selected subtitle track option.
     *
     * @return Current SubtitleTrackOption, or null if no subtitle tracks available
     */
    fun getCurrentSubtitleTrack(): SubtitleTrackOption? = currentSubtitleTrack

    /**
     * Check if subtitles are currently enabled.
     *
     * @return true if a subtitle track is selected, false otherwise
     */
    fun isSubtitleEnabled(): Boolean = currentSubtitleTrack != null

    /**
     * Check if alternative audio tracks are available.
     *
     * @return true if more than one audio track is available
     */
    fun hasAlternativeAudio(): Boolean {
        return player.audioTrackCount > 1
    }

    /**
     * Check if alternative subtitle tracks are available.
     *
     * @return true if more than one subtitle track is available
     */
    fun hasAlternativeSubtitles(): Boolean {
        return player.subtitleTrackCount > 1
    }
}