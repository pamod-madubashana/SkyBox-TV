/**
 * LoadingState — Compose TV loading indicator composable.
 * Displays a circular progress indicator with an optional message.
 * Designed for D-pad navigation on Android TV.
 */
package com.skybox.tv.ui.components

import androidx.compose.foundation.layout.Center
import androidx.compose.foundation.layout.spacer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.skybox.tv.ui.theme.TvTheme

/**
 * Shows a loading state with a progress indicator and optional message.
 *
 * @param message Optional message to display below the progress indicator
 * @param focusRequester Focus requester for D-pad navigation
 */
@Composable
fun LoadingState(
    message: String = "Loading...",
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    SkyBoxTvTheme {
        Center {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                spacer(height = 8.dp)
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                spacer(height = 16.dp)
            }
        }
    }
}

/**
 * Shows a loading state without a message.
 */
@Composable
fun LoadingStateMinimal(
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    SkyBoxTvTheme {
        Center {
            CircularProgressIndicator(
                modifier = Modifier
                    .focusRequester(focusRequester)
            )
        }
    }
}