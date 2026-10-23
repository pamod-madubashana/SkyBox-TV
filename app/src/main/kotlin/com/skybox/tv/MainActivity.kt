package com.skybox.tv

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.ui.auth.AuthScreen
import com.skybox.tv.ui.theme.SkyBoxTvTheme

/**
 * MainActivity for SkyBox TV.
 * Displays the Telegram authentication screen as the entry point.
 * After authorization, the user can browse chats and play media.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkyBoxTvTheme {
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                AuthScreen(
                    onAuthorized = { account ->
                        // TODO: Navigate to chat browsing screen
                    },
                    onListChats = {
                        // TODO: Show chat list
                    },
                    onLogout = {
                        // TODO: Handle logout
                    }
                )
            }
        }
    }
}