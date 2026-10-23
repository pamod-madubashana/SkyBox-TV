/**
 * ErrorState — Compose TV error state composable.
 * Displays an error icon, message, and retry button.
 * Designed for D-pad navigation on Android TV.
 */
package com.skybox.tv.ui.components

import androidx.compose.foundation.layout.Center
import androidx.compose.foundation.layout.spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skybox.tv.ui.theme.TvTheme

/**
 * Shows an error state with a retry option.
 *
 * @param message Error message to display
 * @param onRetry Callback for retry action
 * @param onBack Optional callback for back action
 * @param focusRequester Focus requester for D-pad navigation
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: (() -> Unit)? = null,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    SkyBoxTvTheme {
        Center {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material3.Icons.Failed,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
                spacer(height = 8.dp)
                Text(
                    text = "Error: $message",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                spacer(height = 16.dp)
                Button(
                    onClick = { onRetry.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .focusRequester(focusRequester)
                ) {
                    Text("Retry")
                }
                if (onBack != null) {
                    spacer(height = 8.dp)
                    Button(
                        onClick = { onBack.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .focusRequester(focusRequester)
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

/**
 * Minimal error state without back button.
 */
@Composable
fun ErrorStateMinimal(
    message: String,
    onRetry: () -> Unit,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    SkyBoxTvTheme {
        Center {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material3.Icons.Failed,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
                spacer(height = 8.dp)
                Text(
                    text = "Error: $message",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                spacer(height = 16.dp)
                Button(
                    onClick = { onRetry.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .focusRequester(focusRequester)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}