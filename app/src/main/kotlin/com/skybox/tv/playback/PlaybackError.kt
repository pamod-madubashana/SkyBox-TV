/**
 * PlaybackError represents different categories of errors that can occur
 * during media playback in SkyBox TV.
 *
 * Pattern: Sealed class for exhaustive error handling - UI can switch on
 * this type to show appropriate error messages and recovery actions.
 *
 * Classification:
 * - NetworkError: Errors related to network connectivity or source access
 * - CodecError: Errors related to media format decoding
 * - SourceError: Errors related to the media source itself
 * - Unknown: Errors that don't fit other categories
 *
 * Retryable vs Terminal:
 * - NetworkError may be retryable if transient
 * - CodecError is typically terminal (unsupported format)
 * - SourceError may be retryable if the source becomes available
 * - Unknown is treated as terminal until more information is available
 */
package com.skybox.tv.playback

/**
 * Sealed class representing playback errors with categorization.
 *
 * UI components can switch on this type to display appropriate messages
 * and offer context-specific recovery actions.
 */
sealed class PlaybackError {

    /** Error related to network connectivity or source access. */
    data class NetworkError(
        /** Description of the network issue */
        val message: String,
        /** Whether this error is retryable (transient network issue) */
        val isRetryable: Boolean = true
    ) : PlaybackError()

    /** Error related to media format decoding (codec not supported, etc.). */
    data class CodecError(
        /** Description of the codec/decoding issue */
        val message: String,
        /** Whether the format might be supported with a different codec */
        val mightBeSupported: Boolean = false
    ) : PlaybackError()

    /** Error related to the media source itself (invalid, corrupted, etc.). */
    data class SourceError(
        /** Description of the source issue */
        val message: String,
        /** Whether the source might become available later */
        val mightBecomeAvailable: Boolean = false
    ) : PlaybackError()

    /** Unknown error that doesn't fit other categories. */
    data class Unknown(
        /** Description of the error */
        val message: String,
        /** Additional context about the error */
        val context: String? = null
    ) : PlaybackError()

    /** Companion object with helper methods for error classification. */
    companion object {

        /**
         * Classify an exception into a PlaybackError category.
         * This determines whether the error is retryable or terminal.
         *
         * @param e The exception to classify
         * @return Appropriate PlaybackError instance
         */
        fun classify(e: Exception): PlaybackError = when (e) {
            is java.io.IOException -> PlaybackError.NetworkError(
                message = "Network error: ${e.message}",
                isRetryable = true
            )
            is java.net.SocketException -> PlaybackError.NetworkError(
                message = "Connection error: ${e.message}",
                isRetryable = true
            )
            is java.net.UnknownHostException -> PlaybackError.NetworkError(
                message = "Host unavailable: ${e.message}",
                isRetryable = false
            )
            is androidx.media3.common.playbackerror.MediaCodecPlaybackException ->
                PlaybackError.CodecError(
                    message = "Codec error: ${e.message}",
                    mightBeSupported = false
                )
            is java.lang.IllegalArgumentException -> PlaybackError.SourceError(
                message = "Invalid source: ${e.message}",
                mightBecomeAvailable = false
            )
            is java.lang.SecurityException -> PlaybackError.Unknown(
                message = "Security error: ${e.message}",
                context = "Permission-related"
            )
            else -> PlaybackError.Unknown(
                message = "Playback error: ${e.message}",
                context = e::class.java.name
            )
        }

        /**
         * Determine if a PlaybackError is retryable.
         * Retryable errors are those that might succeed on a subsequent attempt.
         *
         * @param error The error to check
         * @return true if the error is retryable, false if terminal
         */
        fun isRetryable(error: PlaybackError): Boolean = when (error) {
            is PlaybackError.NetworkError -> error.isRetryable
            is PlaybackError.CodecError -> false // Codec errors are typically terminal
            is PlaybackError.SourceError -> error.mightBecomeAvailable
            is PlaybackError.Unknown -> false // Unknown errors are terminal until investigated
        }

        /**
         * Get a user-friendly message for the error.
         *
         * @param error The error to get message for
         * @return Human-readable error message
         */
        fun getUserMessage(error: PlaybackError): String = when (error) {
            is PlaybackError.NetworkError -> {
                if (error.isRetryable) {
                    "Network issue. Retrying..."
                } else {
                    "Network issue. Please check your connection."
                }
            }
            is PlaybackError.CodecError -> "Unsupported media format."
            is PlaybackError.SourceError -> "Unable to load media source."
            is PlaybackError.Unknown -> error.message ?: "An unknown error occurred."
        }

        /**
         * Get an action label for recovery from the error.
         *
         * @param error The error to get action for
         * @return Label for the recovery action button
         */
        fun getRecoveryAction(error: PlaybackError): String = when (error) {
            is PlaybackError.NetworkError -> if (error.isRetryable) "Retry" else "Check Connection"
            is PlaybackError.CodecError -> "Try Different File"
            is PlaybackError.SourceError -> "Try Again"
            is PlaybackError.Unknown -> "Retry"
        }
    }
}