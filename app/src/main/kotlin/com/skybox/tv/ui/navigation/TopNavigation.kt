package com.skybox.tv.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.navigation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.isFocusable
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.navigation.navigation
import androidx.compose.ui.navigation.setDestinationState
import androidx.compose.ui.navigation.navigate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skybox.tv.MainActivity
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.auth.AuthScreen
import com.skybox.tv.ui.chats.ChatListScreen
import com.skybox.tv.ui.home.HomeScreen
import com.skybox.tv.ui.library.LibraryScreen
import com.skybox.tv.ui.player.PlayerScreen
import com.skybox.tv.ui.sources.SourcesScreen
import com.skybox.tv.ui.settings.SettingsScreen
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

/**
 * Top navigation rail for SkyBox TV.
 * Provides D-pad-navigable access to the five main destinations:
 * Home, Library, Sources, Search, and Settings.
 * Handles focus management and restoration when returning from the player.
 */
@Composable
fun TopNavigation(
    authenticated: Boolean = false,
    onSignOut: () -> Unit = {},
    destinationState: NavHostController = rememberNavController(),
    onDestinationChanged: (Destination) -> Unit = {},
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    // Track the currently focused destination for restoration
    val currentlyFocusedDestination by remember { mutableStateOf(Destination.Home) }

    // Request focus based on authentication state
    LaunchedEffect(authenticated) {
        if (authenticated) {
            focusRequester.requestFocus()
        }
    }

    SkyBoxTvTheme {
        NavHost(
            navController = destinationState,
            startDestination = Destination.Home.route,
            modifier = Modifier.fillMaxSize(),
            focusRequester = focusRequester
        ) {
            // Home destination
            composable(route = Destination.Home.route) {
                HomeScreen(
                    focusRequester = focusRequester,
                    onDestinationChanged = { onDestinationChanged(it) }
                )
            }

            // Library destination
            composable(route = Destination.Library.route) {
                LibraryScreen(
                    focusRequester = focusRequester,
                    onDestinationChanged = { onDestinationChanged(it) }
                )
            }

            // Sources destination
            composable(route = Destination.Sources.route) {
                SourcesScreen(
                    focusRequester = focusRequester,
                    onBack = { navigateBack(destinationState) }
                )
            }

            // Search destination
            composable(route = Destination.Search.route) {
                SearchScreen(
                    focusRequester = focusRequester,
                    onDestinationChanged = { onDestinationChanged(it) }
                )
            }

            // Settings destination
            composable(route = Destination.Settings.route) {
                SettingsScreen(
                    focusRequester = focusRequester,
                    onBack = { navigateBack(destinationState) }
                )
            }
        }

        // Bottom navigation rail for TV
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigation(
                    modifier = Modifier.fillMaxWidth(),
                    selectedIndex = currentlyFocusedDestination.ordinal,
                    onSelect = { index ->
                        val destination = when (index) {
                            0 -> Destination.Home
                            1 -> Destination.Library
                            2 -> Destination.Sources
                            3 -> Destination.Search
                            4 -> Destination.Settings
                            else -> Destination.Home
                        }
                        onDestinationChanged(destination)
                        currentlyFocusedDestination = destination
                    }
                ) {
                    Destination.Home -> {
                        MaterialIconButton(
                            enabled = true,
                            onClick = {},
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material3.Icons.Home,
                                contentDescription = "Home"
                            )
                        }
                    }
                    Destination.Library -> {
                        MaterialIconButton(
                            enabled = true,
                            onClick = {},
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material3.Icons.LibraryBooks,
                                contentDescription = "Library"
                            )
                        }
                    }
                    Destination.Sources -> {
                        MaterialIconButton(
                            enabled = true,
                            onClick = {},
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material3.Icons.ContentCopy,
                                contentDescription = "Sources"
                            )
                        }
                    }
                    Destination.Search -> {
                        MaterialIconButton(
                            enabled = true,
                            onClick = {},
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material3.Icons.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                    Destination.Settings -> {
                        MaterialIconButton(
                            enabled = true,
                            onClick = {},
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material3.Icons.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            }
        )
    }
}

/** Navigation destinations for the top-level navigation. */
enum class Destination {
    Home("home"),
    Library("library"),
    Sources("sources"),
    Search("search"),
    Settings("settings");

    val route: String
    init {
        route = when (this) {
            Destination.Home -> "home"
            Destination.Library -> "library"
            Destination.Sources -> "sources"
            Destination.Search -> "search"
            Destination.Settings -> "settings"
        }
    }
}

/** Navigate back to previous destination. */
private fun navigateBack(navController: NavHostController) {
    if (navController.currentDestination != null && navController.currentDestination!.route != Destination.Home.route) {
        navController.popBack_stack()
    } else {
        navController.navigate(Destination.Home.route)
    }
}

/** Search screen composable. */
@Composable
fun SearchScreen(
    focusRequester: FocusRequester,
    onDestinationChanged: (Destination) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .focusRequester(focusRequester),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(16.dp)
        )
        // Search UI would go here
        Text(
            text = "Search for movies and videos",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}