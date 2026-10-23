/**
 * EmptyState — Compose TV empty state composable.
 * Displays an icon and message when no data is available.
 * Designed for D-pad navigation on Android TV.
 */
package com.skybox.tv.ui.components

import androidx.compose.foundation.layout.Center
import androidx.compose.foundation.layout.spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.image.IconVector
import androidx.compose.ui.image painter
import androidx.compose.ui.unit.dp
import com.skybox.tv.ui.theme.TvTheme

/**
 * Shows an empty state with an optional icon, message, and action button.
 *
 * @param iconResource Optional icon resource ID (default: account circle)
 * @param title Title text to display
 * @param description Description text to display
 * @param actionText Optional action button text (e.g., "Add Chats")
 * @param onAction Optional action to perform when button is clicked
 * @param focusRequester Focus requester for D-pad navigation
 */
@Composable
fun EmptyState(
    iconResource: Int = androidx.compose.material3.Icons.Outline.AccountCircle,
    title: String = "No Items",
    description: String = "No content available.\nStart adding media to your library.",
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
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
                    imageVector = iconResource,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
                spacer(height = 8.dp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                spacer(height = 4.dp)
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    textAlign = androidx.compose.ui.Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                spacer(height = 16.dp)
                if (actionText != null) {
                    Button(
                        onClick = { onAction?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .focusRequester(focusRequester)
                    ) {
                        Text(actionText)
                    }
                }
            }
        }
    }
}

/**
 * Minimal empty state without action button.
 */
@Composable
fun EmptyStateMinimal(
    title: String = "No Items",
    description: String = "",
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
                    imageVector = androidx.compose.material3.Icons.Outline.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
                spacer(height = 8.dp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (description.isNotEmpty()) {
                    spacer(height = 4.dp)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}