package com.skybox.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme

/**
 * MainActivity for SkyBox TV.
 * Sets up a Compose for TV screen with D-pad navigable buttons.
 * Demonstrates focusable elements in a column layout for remote control navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkyBoxTvTheme {
                // Initialize view system for TV focus navigation
                val view = LocalView.current
                val context = LocalContext.current

                // Create focus requester for D-pad navigation
                val focusRequester = remember { FocusRequester() }

                // Request initial focus on the first button
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                // UI using TvTheme from AndroidX TV
                SkyBoxTvTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .focusRequester(focusRequester),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    ) {
                        // Title text at the top
                        Text(
                            text = R.string.title,
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .semantics { clickable; setOnClick { /* handled via D-pad */ } }
                        )

                        // Spacer to push buttons down
                        spacer(height = 48.dp)

                        // Focusable buttons for D-pad navigation
                        button(
                            onClick = { /* Handle resume */ },
                            modifier = Modifier
                                .focusRequester(FocusRequester())
                        ) {
                            Text(text = R.string.btn_resume)
                        }

                        button(
                            onClick = { /* Handle library */ },
                            modifier = Modifier
                                .focusRequester(FocusRequester())
                        ) {
                            Text(text = R.string.btn_library)
                        }

                        button(
                            onClick = { /* Handle settings */ },
                            modifier = Modifier
                                .focusRequester(FocusRequester())
                        ) {
                            Text(text = R.string.btn_settings)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable button for TV focusable elements.
 * Wraps a Button with proper focus requester management for D-pad navigation.
 */
@Composable
fun button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    modifier = modifier.focusRequester(focusRequester)
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
    ) {
        // Button content is provided by the caller
    }
}

/**
 * SkyBox TvTheme composition local.
 * Sets up TV-optimized theming using AndroidX TvTheme.
 */
@Composable
fun SkyBoxTvTheme(
    content: @Composable () -> Unit
) {
    // Use TvTheme from AndroidX which provides TV-optimized defaults
    androidx.tv.material.TvTheme(
        // Content background matches system TV background
        content = content
    )
}