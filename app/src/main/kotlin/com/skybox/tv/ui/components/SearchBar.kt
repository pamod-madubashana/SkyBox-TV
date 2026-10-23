package com.skybox.tv.ui.components

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pointer.keyboardKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.ui.theme.TvTheme
import kotlin.coroutines.resumeWithException

/**
 * TV-friendly search input field.
 * Designed for D-pad remote control navigation.
 * Supports debounced searching via callback.
 *
 * @param value Current search text value
 * @param onValueChanged Called when user types
 * @param placeholder/hint text shown when empty
 * @param onSearchPerformed Called when user submits search (debounced)
 * @param focusRequester Focus requester for D-pad navigation
 */
@Composable
fun SearchBar(
    value: String,
    onValueChanged: (String) -> Unit,
    placeholder: String = "Search...",
    onSearchPerformed: (String) -> Unit = {},
    focusRequester: androidx.compose.ui.focus.FocusRequester
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .focusRequester(focusRequester),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterStart
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            label = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.TEXT,
                autoCorrect = false
            ),
            singleLine = true,
            imeAction = ImeAction.Search,
            onImeActionPerformed = {
                onSearchPerformed(value)
                true
            }
        )
    }
}

/**
 * Search chip/button that appears after text input.
 * Shows clear action and initiates search.
 */
@Composable
fun SearchActionChip(
    searchText: String,
    onClear: () -> Unit,
    onSearch: (String) -> Unit,
    isSearching: Boolean = false
) {
    if (!searchText.isEmpty()) {
        Chip(
            value = searchText,
            onDelete = onClear,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            style = ChipStyle.Filled,
            onCloseClick = {}
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(16.dp)
                )
            }
            Text(
                text = searchText,
                modifier = Modifier
                    .padding(start = 4.dp)
            )
        }
    } else {
        OutlinedButton(
            onClick = { onSearch(searchText) },
            modifier = Modifier
                .wrapContentWidth()
                .padding(4.dp),
            style = OutlinedButtonStyle(
                shape = RoundedCornerShape(4f)
            )
        ) {
            Text("Search")
        }
    }
}