package com.skybox.tv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Keys
import androidx.compose.ui.input.key.displayKeyEvent
import androidx.compose.ui.unit.dp
import com.skybox.tv.data.streaming.TelegramMediaDataSource
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.presentation.player.PlaybackSessionState
import com.skybox.tv.presentation.player.PlayerLifecycleManager
import kotlin.runBlocking

/**
 * Action buttons for playback control: Play, Resume, Restart.
 * Arranged horizontally for TV D-pad navigation.
 *
 * @param mediaItem The media item to play/restart/resume
 * @param onPlay Callback to start playback (with dataSource)
 * @param onResume Callback to resume playback from saved position
 * @param onRestart Callback to restart from beginning
 * @param currentState Current playback session state
 * @param dataSource Optional TelegramMediaDataSource
 */
@Composable
fun ActionButtons(
    mediaItem: MediaItem,
    onPlay: (MediaItem, TelegramMediaDataSource?) -> Unit,
    onResume: (MediaItem, TelegramMediaDataSource?) -> Unit,
    onRestart: (MediaItem, TelegramMediaDataSource?) -> Unit,
    currentState: PlaybackSessionState? = null,
    dataSource: TelegramMediaDataSource? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalArrangement = Arrangement.SpaceEvenly,
        navigationMode = NavigationMode.Horizontal
    ) {
        // Play button - shown when no playback or stopped state
        if (currentState == null || currentState == PlaybackSessionState.Idle) {
            PlayButton(
                mediaItem = mediaItem,
                onPlay = { onPlay(mediaItem, dataSource) },
                focusRequester = remember { FocusRequester() }
            )
        }
        // Resume button - shown when playback was paused or stopped with progress
        else if (currentState == PlaybackSessionState.Paused
            || currentState == PlaybackSessionState.Completed) {
            ResumeButton(
                mediaItem = mediaItem,
                onResume = { onResume(mediaItem, dataSource) },
                focusRequester = remember { FocusRequester() }
            )
        }
        // Restart button - shown when playback is stopped but has progress
        else if (currentState == PlaybackSessionState.Stopped) {
            RestartButton(
                mediaItem = mediaItem,
                onRestart = { onRestart(mediaItem, dataSource) },
                focusRequester = remember { FocusRequester() }
            )
        }
    }
}

/** Play button - starts playback from beginning. */
@Composable
private fun PlayButton(
    mediaItem: MediaItem,
    onPlay: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedButton(
        onClick = onPlay,
        modifier = Modifier
            .size(80.dp)
            .focusRequester(focusRequester),
        style = OutlinedButtonStyle(
            shape = RoundedCornerShape(8f)
        )
    ) {
        Text(
            text = "Play",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** Resume button - resumes from saved position. */
@Composable
private fun ResumeButton(
    mediaItem: MediaItem,
    onResume: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedButton(
        onClick = onResume,
        modifier = Modifier
            .size(80.dp)
            .focusRequester(focusRequester),
        style = OutlinedButtonStyle(
            shape = RoundedCornerShape(8f)
        )
    ) {
        Text(
            text = "Resume",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** Restart button - restarts from beginning. */
@Composable
private fun RestartButton(
    mediaItem: MediaItem,
    onRestart: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedButton(
        onClick = onRestart,
        modifier = Modifier
            .size(80.dp)
            .focusRequester(focusRequester),
        style = OutlinedButtonStyle(
            shape = RoundedCornerShape(8f)
        )
    ) {
        Text(
            text = "Restart",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}