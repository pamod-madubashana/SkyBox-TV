/**
 * SubtitleManager handles subtitle display and tracking during playback.
 * This class works with ExoPlayer's text track selection to provide
 * subtitle support for media content from Telegram.
 *
 * Pattern: Mediator pattern between ExoPlayer text tracks and UI subtitle view.
 *
 * Responsibilities:
 * - Manage subtitle track selection and switching
 * - Provide subtitle text to UI components
 * - Handle subtitle language selection
 * - Track subtitle enable/disable state
 * - Provide cleanup and release operations
 *
 * Note: Subtitle rendering (displaying text on screen) is handled by the UI layer.
 * This manager provides the subtitle data and selection logic; the UI decides
 * how and where to render subtitles.
 */
package com.skybox.tv.playback

import androidx.media3.common.Player
import androidx.media3.common.text.Subtitle
import androidx.media3.common.text.SubtitleInputObject
import androidx.media3.common.text.SubtitleOutput
import androidx.media3.common.text.SubtitleStyle
import androidx.media3.exoplayer.ExoPlayer
import com.skybox.tv.domain.model.MediaItem

/**
 * Options for subtitle configuration.
 */
data class SubtitleOptions(
    /** Whether subtitles are enabled */
    val enabled: Boolean = true,
    /** Preferred subtitle language code (e.g., "en", "es", "fr"), or null for any */
    val preferredLanguage: String? = null,
    /** Whether to show forced-only subtitles (subtitles that convey important audio info) */
    val forcedOnly: Boolean = false,
    /** Subtitle scale factor for rendering */
    val scale: Float = 1.0f,
    /** Subtitle color for rendering */
    val color: String = "#FFFFFF",
    /** Background opacity for subtitle rendering */
    val backgroundOpacity: Float = 0.5f
)

/**
 * State of the subtitle manager.
 */
sealed class SubtitleState {

    /** Subtitles are enabled and a track is selected. */
    data class Enabled(val trackIndex: Int, val languageCode: String?) : SubtitleState()

    /** Subtitles are disabled. */
    object Disabled : SubtitleState()

    /** No subtitle tracks are available for the current media. */
    object NoTracksAvailable : SubtitleState()

    /** Subtitles are enabled but language is unknown. */
    data class LanguageUnknown(val trackIndex: Int) : SubtitleState()
}

/**
 * SubtitleManager handles subtitle track selection and provides subtitle text
 * to the UI layer for rendering.
 *
 * This class abstracts ExoPlayer's text track operations and provides a simpler
 * interface for UI components to control subtitle display.
 *
 * UI Integration:
 * - The UI observes SubtitleState changes
 * - The UI calls selectSubtitleLanguage() or setSubtitleOptions() to change tracks
 * - The UI requests subtitle text via getCurrentSubtitle() when needing to render
 * - The UI calls release() when the player changes
 */
class SubtitleManager(
    private val player: Player,
    private val subtitleOptions: SubtitleOptions = SubtitleOptions()
) {

    /** Current subtitle state. */
    private var subtitleState: SubtitleState = SubtitleState.Disabled

    /** Current subtitle options. */
    private var currentOptions: SubtitleOptions = subtitleOptions

    /** Last retrieved subtitle text, for observation. */
    private var lastSubtitleText: String? = null

    /** Last subtitle display time, for caching. */
    private var lastSubtitleTimeMs: Long = 0

    /**
     * Get the current subtitle state.
     *
     * @return Current SubtitleState (Enabled, Disabled, NoTracksAvailable, LanguageUnknown)
     */
    fun getSubtitleState(): SubtitleState = subtitleState

    /**
     * Get available subtitle tracks for the current media.
     *
     * @return List of available subtitle track indices and languages
     */
    @UnstableApi
    fun getAvailableSubtitleTracks(): List<SubtitleTrackOption> {
        val result = mutableListOf<SubtitleTrackOption>()

        val subtitleTrackCount = player.subtitleTrackCount
        if (subtitleTrackCount <= 1) {
            return result
        }

        // Query available text tracks from ExoPlayer
        for (i in 0 until subtitleTrackCount) {
            try {
                val trackInfo = player.getTextSelection(i)
                val language = trackInfo.language
                val name = if (language != null) language : "Subtitle $i"

                // Check if this track matches preferred language
                val matchesPreferred = currentOptions.preferredLanguage == null ||
                    language == currentOptions.preferredLanguage

                result.add(SubtitleTrackOption(
                    trackIndex = i,
                    languageCode = language,
                    name = name,
                    isDefault = trackInfo.isDefault,
                    isForced = trackInfo.isForced
                ))
            } catch (e: Exception) {
                // Skip tracks that can't be queried
            }
        }

        return result
    }

    /**
     * Select a subtitle track by language code.
     *
     * @param languageCode Language code to select (e.g., "en", "es"), or null for default
     * @return TrackSelectionResult indicating success or failure
     */
    @UnstableApi
    fun selectSubtitleLanguage(languageCode: String?): TrackSelectionResult {
        val availableTracks = getAvailableSubtitleTracks()
        val selectedTrack = availableTracks.find { it.languageCode == languageCode }

        return if (selectedTrack != null) {
            selectSubtitleTrack(selectedTrack.trackIndex)
        } else if (languageCode == null && availableTracks.isNotEmpty()) {
            // Select first available track as default
            selectSubtitleTrack(availableTracks.first().trackIndex)
        } else {
            TrackSelectionResult.Failure(
                reason = "No subtitle track found with language code: $languageCode"
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
            val availableTracks = getAvailableSubtitleTracks()
            val selectedTrack = availableTracks.find { it.trackIndex == trackIndex }

            if (selectedTrack != null) {
                subtitleState = SubtitleState.Enabled(
                    trackIndex = selectedTrack.trackIndex,
                    languageCode = selectedTrack.languageCode
                )
                lastSubtitleText = null // Clear cached text
                return TrackSelectionResult.Success(
                    trackOption = selectedTrack,
                    wasNewlySelected = true
                )
            } else {
                return TrackSelectionResult.Failure(
                    reason = "Subtitle track $trackIndex not available"
                )
            }
        } catch (e: Exception) {
            return TrackSelectionResult.Failure(
                reason = "Failed to select subtitle track: ${e.message}"
            )
        }
    }

    /**
     * Enable or disable subtitles.
     *
     * @param enabled Whether to enable subtitles
     * @return TrackSelectionResult indicating success or failure
     */
    @UnstableApi
    fun setSubtitleEnabled(enabled: Boolean): TrackSelectionResult {
        if (enabled) {
            // Enable subtitles - select the default or first available track
            val availableTracks = getAvailableSubtitleTracks()
            if (availableTracks.isNotEmpty()) {
                // Try to select the default track first, otherwise first available
                val defaultTrack = availableTracks.find { it.isDefault } ?: availableTracks.first()
                return selectSubtitleTrack(defaultTrack.trackIndex)
            } else {
                return TrackSelectionResult.Failure(
                    reason = "No subtitle tracks available to enable"
                )
            }
        } else {
            // Disable subtitles
            player.setSelectedTextTrackIndex(Player.DEFAULT)
            subtitleState = SubtitleState.Disabled
            lastSubtitleText = null
            return TrackSelectionResult.Success(
                trackOption = SubtitleTrackOption(trackIndex = -1, languageCode = null, name = ""),
                wasNewlySelected = true
            )
        }
    }

    /**
     * Get the current subtitle text for rendering.
     *
     * This returns the most recent subtitle text from ExoPlayer.
     * UI should call this when needing to display a subtitle update.
     *
     * @return Subtitle text, or null if no subtitle currently active
     */
    @UnstableApi
    fun getCurrentSubtitle(): String? {
        // Try to get the latest subtitle from the player
        val subtitleCount = player.textTrackSelection?.textTracks?.size ?: 0
        if (subtitleCount > 0) {
            // Get the currently selected text track
            val selectedIndex = player.selectedTextTrackIndex
            if (selectedIndex >= 0 && selectedIndex < subtitleCount) {
                val textTrack = player.textTrackSelection?.textTracks[selectedIndex]
                if (textTrack != null) {
                    // Update last known text
                    lastSubtitleText = textTrack.text
                    return textTrack.text
                }
            }
        }

        return lastSubtitleText
    }

    /**
     * Get current subtitle options.
     *
     * @return Current SubtitleOptions
     */
    fun getSubtitleOptions(): SubtitleOptions = currentOptions

    /**
     * Update subtitle options (language, enabled, etc.).
     *
     * @param options New subtitle options
     */
    fun updateOptions(options: SubtitleOptions) {
        currentOptions = options
        // If a preferred language was set, try to select it
        if (options.preferredLanguage != null) {
            selectSubtitleLanguage(options.preferredLanguage)
        }
    }

    /**
     * Release resources held by the subtitle manager.
     * Should be called when the player changes or is released.
     */
    fun release() {
        // Reset to disabled state
        subtitleState = SubtitleState.Disabled
        lastSubtitleText = null
        currentOptions = subtitleOptions
    }
}