package com.skybox.tv.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.unit.dp
import com.skybox.tv.R
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme

/**
 * Settings screen for SkyBox TV.
 * Provides options for account management, playback settings,
 * cache management, and diagnostics.
 * Designed for D-pad navigation on Android TV.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = SettingsViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state
    val focusRequester = remember { FocusRequester() }

    // Request initial focus based on state
    LaunchedEffect(Unit) {
        when (state) {
            is SettingsState.Loading -> focusRequester.requestFocus()
            is SettingsState.Error -> focusRequester.requestFocus()
        }
    }

    SkyBoxTvTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .focusRequester(focusRequester),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            Text(
                text = "Settings",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .semantics { trueLabel = true }
            )

            spacer(height = 8.dp)

            // Account section
            settingsSection(
                title = "Account",
                focusRequester = focusRequester
            ) {
                AccountSettingsContent(
                    authState = viewModel.authState,
                    onLogout = viewModel::logout,
                    focusRequester = focusRequester
                )
            }

            spacer(height = 8.dp)

            // Playback section
            settingsSection(
                title = "Playback",
                focusRequester = focusRequester
            ) {
                PlaybackSettingsContent(
                    cacheSize = viewModel.cacheSize,
                    onCacheSizeChanged = viewModel::setCacheSize,
                    focusRequester = focusRequester
                )
            }

            spacer(height = 8.dp)

            // Cache section
            settingsSection(
                title = "Cache",
                focusRequester = focusRequester
            ) {
                CacheSettingsContent(
                    currentCacheSize = viewModel.currentCacheSize,
                    onClearCache = viewModel::clearCache,
                    focusRequester = focusRequester
                )
            }

            spacer(height = 8.dp)

            // Diagnostics section
            settingsSection(
                title = "Diagnostics",
                focusRequester = focusRequester
            ) {
                DiagnosticsContent(focusRequester = focusRequester)
            }
        }
    }
}

/** Settings section wrapper with title and content. */
@Composable
private fun settingsSection(
    title: String,
    focusRequester: FocusRequester,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Start,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                .semantics { trueLabel = true }
        )

        content()
    }
}

/** Account settings content. */
@Composable
private fun AccountSettingsContent(
    authState: StateFlow<AuthState>,
    onLogout: () -> Unit,
    focusRequester: FocusRequester
) {
    val authStateValue = authState.value

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        // Session status
        when (authStateValue) {
            is AuthState.Authorized -> {
                Text(
                    text = "Session: Authorized",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.fillMaxWidth()
                )

                spacer(height = 8.dp)

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                ) {
                    Text("Logout")
                }
            }
            is AuthState.LoggingOut -> {
                Center {
                    Text("Logging out from Telegram...")
                    spacer(height = 8.dp)
                    CircularProgressIndicator()
                }
            }
            else -> {
                Text(
                    text = "Session: Not authorized",
                    style = MaterialTheme.typography.body1.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                spacer(height = 8.dp)

                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    enabled = false
                ) {
                    Text("Login")
                }
            }
        }
    }
}

/** Playback settings content. */
@Composable
private fun PlaybackSettingsContent(
    cacheSize: Int,
    onCacheSizeChanged: (Int) -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        // Cache size selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cache size:",
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${cacheSize} MB",
                    style = MaterialTheme.typography.body1
                )

                Slider(
                    value = cacheSize.toFloat(),
                    onValueChange = { onCacheSizeChanged(it.roundToInt()) },
                    modifier = Modifier.width(200.dp)
                )
            }
        }

        spacer(height = 8.dp)

        // Quality preference
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Video quality:",
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "High",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.width(80.dp)
                )

                Slider(
                    value = 3f,
                    onValueChange = {},
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

/** Cache settings content. */
@Composable
private fun CacheSettingsContent(
    currentCacheSize: Long,
    onClearCache: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        // Current cache size
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current cache size:",
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${currentCacheSize / 1024 / 1024} MB",
                style = MaterialTheme.typography.body1
            )
        }

        spacer(height = 8.dp)

        // Clear cache button
        Button(
            onClick = onClearCache,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "Clear Cache",
                style = MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colorScheme.onError
                )
            )
        }
    }
}

/** Diagnostics content. */
@Composable
private fun DiagnosticsContent(focusRequester: FocusRequester) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        // Version info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Version:",
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "1.0.0",
                style = MaterialTheme.typography.body1
            )
        }

        spacer(height = 8.dp)

        // Build info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Build:",
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Oct 2026",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

/** ViewModel managing the settings state. */
class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow<SettingsState>(SettingsState.Idle)
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    // Playback settings
    var cacheSize by mutableStateOf(256) // in MB
        private set // Would persist to DataStore in real app

    // Cache stats
    var currentCacheSize by mutableStateOf(128L) // in bytes
        private set

    // Auth state from repository
    val authState: StateFlow<AuthState>

    init {
        // In real app, would initialize authState from repository
        // For now, default to not authorized
        _state.value = SettingsState.Idle
    }

    fun logout() {
        _state.value = SettingsState.LoggingOut
        // TODO: Call repository logout
    }

    fun setCacheSize(size: Int) {
        cacheSize = size
    }

    fun clearCache() {
        // TODO: Clear ExoPlayer cache and Room cache entries
        currentCacheSize = 0L
    }
}

/** State representation for the settings screen. */
private sealed class SettingsState {

    data object Idle : SettingsState()
    data class LoggingOut : SettingsState()
    data object Loading : SettingsState()
    data class Error(val message: String) : SettingsState()
}