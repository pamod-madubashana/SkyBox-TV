package com.skybox.tv.ui.sources

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.unit.dp
import com.skybox.tv.R
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.data.telegram.TdApi
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme

/**
 * Sources screen showing Telegram chats that can be used as video sources.
 * Users can enable/disable chats to include/exclude them from the library.
 * Shows chat name, last indexed message, and indexing status.
 * Designed for D-pad navigation on Android TV.
 */
@Composable
fun SourcesScreen(
    viewModel: SourcesViewModel = SourcesViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state
    val focusRequester = remember { FocusRequester() }

    // Request initial focus based on state
    LaunchedEffect(Unit) {
        when (state) {
            is SourcesState.Loading -> focusRequester.requestFocus()
            is SourcesState.Error -> focusRequester.requestFocus()
            is SourcesState.Empty -> focusRequester.requestFocus()
            is SourcesState.ChatList -> focusRequester.requestFocus()
        }
    }

    SkyBoxTvTheme {
        when (state) {
            is SourcesState.Loading -> {
                loadingScreen(focusRequester)
            }

            is SourcesState.Error -> {
                errorScreen(
                    message = (state as SourcesState.Error).message,
                    onRetry = viewModel::refreshSources,
                    focusRequester = focusRequester
                )
            }

            is SourcesState.Empty -> {
                emptyScreen(focusRequester)
            }

            is SourcesState.ChatList -> {
                chatListScreen(
                    chats = (state as SourcesState.ChatList).chats,
                    onToggleSource = viewModel::toggleSource,
                    onChatSelected = { chat ->
                        // TODO: Navigate to player or details
                    },
                    focusRequester = focusRequester
                )
            }
        }
    }
}

/** State representation for the sources screen. */
private sealed class SourcesState {

    data object Loading : SourcesState()
    data class Error(val message: String) : SourcesState()
    data object Empty : SourcesState()
    data class ChatList(val chats: List<TdApi.Chat>) : SourcesState()
}

/** ViewModel managing the sources state and chat data. */
class SourcesViewModel : ViewModel() {
    private val _state = MutableStateFlow<SourcesState>(SourcesState.Loading)
    val state: StateFlow<SourcesState> = _state.asStateFlow()

    private val tdLibClient: TdLibClient? = null // Injected in real app
    private val telegramRepository: TelegramRepository? = null // Injected in real app

    init {
        loadChats()
    }

    private fun loadChats() {
        _state.value = SourcesState.Loading
        viewModelScope.launch {
            // TODO: Use telegramRepository to request chats with media
            // For now, simulate and transition
            awaitDelay(500L)
            // In real app: telegramRepository?.requestChats() then filter chats with media
            _state.value = SourcesState.Empty
        }
    }

    fun refreshSources() {
        loadChats()
    }

    /** Toggle a chat as a source (enable/disable) */
    fun toggleSource(chatId: String) {
        // TODO: Toggle isEnabled in MediaSourceEntity via repository
        // This would update the source status in the local database
    }
}

/** Loading state UI. */
@Composable
private fun loadingScreen(focusRequester: FocusRequester) {
    Center {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            spacer(height = 8.dp)
            Text("Loading sources...")
            spacer(height = 16.dp)
            Button(
                onClick = {},
                modifier = Modifier.focusRequester(focusRequester)
            ) {
                Text("Retry")
            }
        }
    }
}

/** Error state UI. */
@Composable
private fun errorScreen(
    message: String,
    onRetry: () -> Unit,
    focusRequester: FocusRequester
) {
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
                tint = Color.Unspecified
            )
            spacer(height = 8.dp)
            Text(
                text = "Error: $message",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.width(200.dp)
            )
            spacer(height = 16.dp)
            Button(onClick = onRetry, modifier = Modifier.focusRequester(focusRequester)) {
                Text("Retry")
            }
        }
    }
}

/** Empty state UI - no chats found. */
@Composable
private fun emptyScreen(focusRequester: FocusRequester) {
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
                tint = colorScheme.onSurface
            )
            spacer(height = 8.dp)
            Text(
                text = "No Chats Found",
                style = MaterialTheme.typography.titleMedium
            )
            spacer(height = 16.dp)
            Text(
                text = "No video chats found in your selected Telegram chats.\nAdd some chats with videos to get started.",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(300.dp)
            )
            spacer(height = 24.dp)
            Button(
                onClick = {},
                modifier = Modifier.focusRequester(focusRequester)
            ) {
                Text("Add Chats")
            }
        }
    }
}

/** Main sources display with focusable items. */
@Composable
private fun chatListScreen(
    chats: List<TdApi.Chat>,
    onToggleSource: (String) -> Unit,
    onChatSelected: (TdApi.Chat) -> Unit,
    focusRequester: FocusRequester
) {
    if (chats.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start,
        navigationMode = NavigationMode.Horizontal
    ) {
        // Header row
        Text(
            text = "Sources",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .semantics { trueLabel = true }
        )

        spacer(height = 8.dp)

        // Chat items
        ChatItemRow(
            chats = chats,
            onToggleSource = onToggleSource,
            onChatSelected = onChatSelected,
            focusRequester = focusRequester
        )
    }
}

/** Row of chat source items focused horizontally. */
@Composable
private fun ChatItemRow(
    chats: List<TdApi.Chat>,
    onToggleSource: (String) -> Unit,
    onChatSelected: (TdApi.Chat) -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.SpaceEvenly,
        navigationMode = NavigationMode.Horizontal
    ) {
        chats.forEach { chat ->
            ChatItem(
                chat = chat,
                onSelect = onChatSelected,
                onToggleSource = onToggleSource,
                focusRequester = focusRequester
            )
        }
    }
}

/** Individual chat source item UI. */
@Composable
private fun ChatItem(
    chat: TdApi.Chat,
    onSelect: (TdApi.Chat) -> Unit,
    onToggleSource: (String) -> Unit,
    focusRequester: FocusRequester
) {
    // Determine if this chat is enabled as a source
    // In a full implementation, this would check the database/repository
    val isSourceEnabled = remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .size(160.dp)
            .focusRequester(focusRequester)
            .semantics {
                selected = isSourceEnabled.value
                clickable {
                    isSourceEnabled.value = !isSourceEnabled.value
                    onToggleSource(chat.id.toString())
                }
                clickableByKeyboard = true
                focusable = true
            },
        elevation = CardElevation.Normal
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterStart
        ) {
            // Chat preview - use title or placeholder
            val chatTitle = when (chat) {
                is TdApi.ChatRecent => {
                    "Chat"
                }
                is TdApi.Chat => chat.title
                else -> "Chat"
            }

            Text(
                text = chatTitle,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            spacer(height = 4.dp)

            // Source status and last indexed message
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Source enable/disable toggle
                Button(
                    onClick = { onToggleSource(chat.id.toString()) },
                    modifier = Modifier.size(32.dp).focusRequester(focusRequester),
                    enabled = true
                ) {
                    val toggleText = if (isSourceEnabled.value) {
                        "✓"
                    } else {
                        "+"
                    }
                    Text(toggleText)
                }

                // Last indexed message info
                Text(
                    text = "Last: ${chat.lastMessageId}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}