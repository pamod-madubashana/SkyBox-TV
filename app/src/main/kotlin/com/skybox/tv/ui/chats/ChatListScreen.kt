package com.skybox.tv.ui.chats

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.GraphicsMode
import androidx.compose.ui.NavigationMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.image painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skybox.tv.R
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.data.telegram.TdApi
import com.skybox.tv.data.telegram.TdLibClient
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme

/**
 * Chat list screen showing available Telegram chats with video content.
 * Uses TDLib to fetch and display chats.
 * Designed for D-pad navigation on Android TV.
 */
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = ChatListViewModel(),
    onChatSelected: (TdApi.Chat) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state = viewModel.state
    val focusRequester = remember { FocusRequester() }

    // Request initial focus based on state
    LaunchedEffect(Unit) {
        when (state) {
            is ChatListState.Loading -> focusRequester.requestFocus()
            is ChatListState.Error -> focusRequester.requestFocus()
            is ChatListState.Empty -> focusRequester.requestFocus()
            is ChatListState.ChatList -> focusRequester.requestFocus()
        }
    }

    SkyBoxTvTheme {
        when (state) {
            is ChatListState.Loading -> {
                loadingScreen(focusRequester)
            }

            is ChatListState.Error -> {
                errorScreen(
                    message = (state as ChatListState.Error).message,
                    onRetry = viewModel::refreshChats,
                    focusRequester = focusRequester
                )
            }

            is ChatListState.Empty -> {
                emptyScreen(focusRequester)
            }

            is ChatListState.ChatList -> {
                chatListScreen(
                    chats = (state as ChatListState.ChatList).chats,
                    onChatSelected = onChatSelected,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

/** State representation for the chat list screen. */
private sealed class ChatListState {

    data object Loading : ChatListState()
    data class Error(val message: String) : ChatListState()
    data object Empty : ChatListState()
    data class ChatList(val chats: List<TdApi.Chat>) : ChatListState()
}

/** ViewModel managing the chat list state. */
class ChatListViewModel : ViewModel() {
    private val _state = MutableStateFlow<ChatListState>(ChatListState.Loading)
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    private val tdLibClient: TdLibClient? = null // Injected in real app

    init {
        loadChats()
    }

    private fun loadChats() {
        _state.value = ChatListState.Loading
        // TODO: Use tdLibClient to request chats
        // For now, simulate a delay and transition
        viewModelScope.launch {
            // Simulate TDLib call
            awaitDelay(1000L)
            // In real app: tdLibClient?.send(TdApi.getChats(limit = 50))
            _state.value = ChatListState.Empty
        }
    }

    fun refreshChats() {
        loadChats()
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
            Text("Loading chats...")
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
                imageVector = Icons.Failed,
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
                imageVector = Icons.Outline.AccountCircle,
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

/** Main chat list display with focusable items. */
@Composable
private fun chatListScreen(
    chats: List<TdApi.Chat>,
    onChatSelected: (TdApi.Chat) -> Unit,
    focusRequester: FocusRequester
) {
    if (chats.isEmpty()) {
        // Fall through to empty state handled elsewhere
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
            text = "Chats",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .semantics { trueLabel = true }
        )

        spacer(height = 8.dp)

        // Chat items in a vertical flow
        ChatItemRow(
            chats = chats,
            onChatSelected = onChatSelected,
            focusRequester = focusRequester
        )
    }
}

/** Row of chat items focused horizontally. */
@Composable
private fun ChatItemRow(
    chats: List<TdApi.Chat>,
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
                focusRequester = focusRequester
            )
        }
    }
}

/** Individual chat item UI. */
@Composable
private fun ChatItem(
    chat: TdApi.Chat,
    onSelect: (TdApi.Chat) -> Unit,
    focusRequester: FocusRequester
) {
    val isSelected = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .size(160.dp)
            .focusRequester(focusRequester)
            .semantics {
                selected = isSelected.value
                clickable {
                    isSelected.value = true
                    onSelect(chat)
                }
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
                    // Get the chat's first message title/name
                    val recent = chat.asTdApi()
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

            // Message count or status
            val recentMessage = when (chat) {
                is TdApi.ChatRecent => "Recent"
                else -> ""
            }

            Text(
                text = recentMessage,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}