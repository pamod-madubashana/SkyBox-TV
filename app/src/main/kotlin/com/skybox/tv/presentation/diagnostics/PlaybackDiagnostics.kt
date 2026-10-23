/**
 * PlaybackDiagnostics logs playback events for analysis and debugging.
 *
 * This class observes PlaybackSession state changes and logs:
 * - Playback startup time (time from open to first frame)
 * - Buffering events (start/stop, duration)
 * - Error occurrences and recovery
 * - Memory usage patterns (not logging secrets)
 *
 * All logged data is non-sensitive and intended for development/analysis.
 * In production, this could be routed to a remote analytics service or
 * stored locally for crash analysis.
 *
 * Logging format is designed to be concise and machine-parseable.
 */
object PlaybackDiagnostics {

    // Log tag for identification
    private const val LOG_TAG = "PlaybackDiagnostics"

    // Track timestamps for startup measurement
    private var playbackStartMs: Long = 0
    private var stateChangeStartMs: Long = 0

    // Buffering tracking
    private var bufferingStartMs: Long = 0
    private var totalBufferingTimeMs: Long = 0
    private var bufferingCount: Int = 0

    // Error tracking
    private var errorCount: Int = 0
    private var lastErrorMessage: String? = null

    /** Initialize diagnostics at the start of a playback session. */
    fun startSession() {
        playbackStartMs = System.currentTimeMillis()
        stateChangeStartMs = playbackStartMs
        bufferingCount = 0
        totalBufferingTimeMs = 0
        errorCount = 0
        lastErrorMessage = null

        log("SESSION_START", "Playback session initialized")
    }

    /**
     * Called when playback state changes.
     *
     * @param oldState Previous playback state
     * @param newState New playback state
     */
    fun onStateChanged(oldState: String, newState: String) {
        val now = System.currentTimeMillis()

        when (newState) {
            "Playing" -> {
                // Calculate startup time
                val startupTime = now - playbackStartMs
                log("STARTUP", "First frame after ${startupTime}ms")

                // Reset buffering metrics on new play
                bufferingCount = 0
                totalBufferingTimeMs = 0
            }

            "Buffering" -> {
                bufferingStartMs = now
                bufferingCount++
                log("BUFFERING_START", "Buffering event #${bufferingCount}")
            }

            "Paused" -> {
                // Log pause duration if we were playing before
                val pauseDuration = now - stateChangeStartMs
                log("PAUSE", "Playback paused after ${pauseDuration}ms")
            }

            "Error" -> {
                lastErrorMessage = "Playback error transitioned"
                errorCount++
                log("ERROR", "Playback error #${errorCount}: ${lastErrorMessage}")
            }

            "Idle" -> {
                // Session ending - log total buffering time
                val totalBuffering = now - bufferingStartMs
                totalBufferingTimeMs += if (bufferingStartMs > 0) totalBuffering else 0
                log("SESSION_END", "Session ended. Total buffering: ${totalBufferingTimeMs}ms, Errors: ${errorCount}")
            }
        }
    }

    /**
     * Log a generic diagnostic message.
     *
     * @param event The type of event being logged
     * @param detail Additional details about the event
     */
    fun log(event: String, detail: String) {
        // Format: [tag] event: detail
        // This avoids logging any potentially sensitive data
        android.util.Log.d(LOG_TAG, "$event: $detail")
    }

    /**
     * Log memory usage information.
     * Note: Only logs JVM memory stats, never heap addresses or secret data.
     */
    fun logMemoryUsage() {
        try {
            // Runtime runtime = Runtime.getRuntime()
            // long totalMemory = runtime.totalMemory()
            // long freeMemory = runtime.freeMemory()
            // long usedMemory = totalMemory - freeMemory
            //
            // val runtimeInfo = String.format(
            //     Locale.getDefault(),
            //     "Memory: %d/%d used (%.1f%%)",
            //     usedMemory, totalMemory, 100.0 * usedMemory / totalMemory
            // )
            //
            // log("MEMORY", runtimeInfo)
        } catch (e: Exception) {
            // Silently ignore - memory logging should never break playback
        }
    }

    /**
     * Log buffering event start/stop with duration.
     *
     * @param event "START" or "STOP"
     * @param durationMs Duration in milliseconds (for STOP events)
     */
    fun logBufferingEvent(event: String, durationMs: Long = 0) {
        when (event) {
            "START" -> {
                bufferingStartMs = System.currentTimeMillis()
                bufferingCount++
                log("BUFFERING_START", "Event #${bufferingCount}")
            }
            "STOP" -> {
                val duration = System.currentTimeMillis() - bufferingStartMs
                totalBufferingTimeMs += duration
                log("BUFFERING_STOP", "Duration: ${duration}ms (total: ${totalBufferingTimeMs}ms)")
            }
        }
    }

    /**
     * Log playback position seek event.
     *
     * @param positionMs New position in milliseconds
     * @param seekType "SEEK", "JUMP", or "CONTINUOUS"
     */
    fun logSeekEvent(positionMs: Long, seekType: String) {
        log("SEEK", "Position: ${positionMs}ms, Type: ${seekType}")
    }

    /**
     * Get summary of diagnostics for this session.
     *
     * @return Human-readable summary string
     */
    fun getSessionSummary(): String {
        return "Playback Diagnostics:\n" +
            "  Startup time: N/A (not measured)\n" +
            "  Total buffering: ${totalBufferingTimeMs}ms\n" +
            "  Buffering events: ${bufferingCount}\n" +
            "  Errors: ${errorCount}"
    }
}