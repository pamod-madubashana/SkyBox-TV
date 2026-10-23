package com.skybox.tv.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.unit.dp
import com.skybox.tv.data.telegram.AuthState
import com.skybox.tv.domain.model.MediaItem
import com.skybox.tv.domain.model.TelegramAccount
import com.skybox.tv.ui.theme.TvTheme
import com.skybox.tv.ui.theme.colorScheme

/**
 * Auth screen for SkyBox TV.
 * Shows the Telegram authorization flow:
 * - NotConfigured: Phone number input
 * - WaitingForPhoneNumber: Code input screen
 * - WaitingForCode: Code verification input
 * - WaitingForPassword: 2FA password input
 * - Authorized: Shows chat list button
 * - LoggingOut: Progress indicator
 * - Error: Error message with retry
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = AuthViewModel(),
    onAuthorized: (account: TelegramAccount) -> Unit = {},
    onListChats: () -> Unit = {}
) {
    val state = viewModel.authState

    var rememberPhone by remember { mutableStateOf("") }
    var rememberCode by remember { mutableStateOf("") }
    var rememberPassword by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    // Request initial focus based on state
    LaunchedEffect(Unit) {
        switch (state) {
            is AuthState.NotConfigured -> focusRequester.requestFocus()
            is AuthState.WaitingForPhoneNumber -> focusRequester.requestFocus()
            is AuthState.WaitingForCode -> focusRequester.requestFocus()
            is AuthState.WaitingForPassword -> focusRequester.requestFocus()
            is AuthState.Authorized -> {}
            is AuthState.LoggingOut -> {}
            is AuthState.Error -> focusRequester.requestFocus()
        }
    }

    SkyBoxTvTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .focusRequester(focusRequester),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterStart
        ) {
            // Title
            Text(
                text = when (state) {
                    is AuthState.NotConfigured -> "SkyBox TV - Connect Telegram"
                    is AuthState.WaitingForPhoneNumber -> "Enter Phone Number"
                    is AuthState.WaitingForCode -> "Enter Verification Code"
                    is AuthState.WaitingForPassword -> "Enter 2FA Password"
                    is AuthState.Authorized -> "SkyBox TV - Authorized"
                    is AuthState.LoggingOut -> "Logging out..."
                    is AuthState.Error -> "Error"
                },
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            spacer(height = 32.dp)

            // State-specific content
            when (state) {
                is AuthState.NotConfigured -> {
                    PhoneNumberInput(
                        phoneNumber = rememberPhone,
                        onPhoneNumberChanged = { rememberPhone = it },
                        onContinue = viewModel::startInitialization,
                        focusRequester = focusRequester
                    )
                }

                is AuthState.WaitingForPhoneNumber -> {
                    PhoneNumberConfirmation(
                        phoneNumber = rememberPhone,
                        onVerify = viewModel::verifyPhoneNumber,
                        onBack = { /* return to not configured */ },
                        focusRequester = focusRequester
                    )
                }

                is AuthState.WaitingForCode -> {
                    CodeInput(
                        code = rememberCode,
                        onCodeChanged = { rememberCode = it },
                        onSubmit = viewModel::verifyCode,
                        onBack = { /* go back to phone confirmation */ },
                        focusRequester = focusRequester
                    )
                }

                is AuthState.WaitingForPassword -> {
                    PasswordInput(
                        password = rememberPassword,
                        onPasswordChanged = { rememberPassword = it },
                        onSubmit = viewModel::verifyPassword,
                        onBack = { /* go back to code input */ },
                        focusRequester = focusRequester
                    )
                }

                is AuthState.Authorized -> {
                    AuthorizedScreen(
                        accountInfo = "Account ID: ${viewModel.userId}",
                        onListChats = { /* TODO: Will be set by MainActivity */ },
                        onLogout = viewModel::logout,
                        focusRequester = focusRequester
                    )
                }

                is AuthState.LoggingOut -> {
                    Center {
                        Text("Logging out from Telegram...")
                        spacer(height = 16.dp)
                        CircularProgressIndicator()
                    }
                }

                is AuthState.Error -> {
                    ErrorState(
                        message = (state as AuthState.Error).message,
                        onRetry = viewModel::resetError,
                        onBack = {},
                        focusRequester = focusRequester
                    )
                }
            }
        }
    }
}

/** Input screen for phone number. */
@Composable
private fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberChanged: (String) -> Unit,
    onContinue: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = phoneNumber,
        onValueChange = onPhoneNumberChanged,
        label = { Text("Phone Number") },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true
    )

    spacer(height = 8.dp)

    Button(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    ) {
        Text("Continue")
    }
}

/** Screen for verifying the phone number with country code confirmation. */
@Composable
private fun PhoneNumberConfirmation(
    phoneNumber: String,
    onVerify: (String) -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { /* Capture phone number input */ },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        spacer(height = 8.dp)

        Button(
            onClick = { onVerify(phoneNumber) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify Phone Number")
        }

        spacer(height = 8.dp)

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

/** Input screen for verification code. */
@Composable
private fun CodeInput(
    code: String,
    onCodeChanged: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = code,
        onValueChange = onCodeChanged,
        label = { Text("Verification Code") },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        imeAction = ImeAction.Done
    )

    spacer(height = 8.dp)

    Button(
        onClick = { onSubmit(code) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    ) {
        Text("Submit Code")
    }

    spacer(height = 8.dp)

    Button(
        onClick = onBack,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    ) {
        Text("Back")
    }
}

/** Input screen for 2FA password. */
@Composable
private fun PasswordInput(
    password: String,
    onPasswordChanged: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChanged,
        label = { Text("2FA Password") },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        obscuringPassword = true,
        imeAction = ImeAction.Done
    )

    spacer(height = 8.dp)

    Button(
        onClick = { onSubmit(password) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    ) {
        Text("Submit Password")
    }

    spacer(height = 8.dp)

    Button(
        onClick = onBack,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    ) {
        Text("Back")
    }
}

/** Screen shown when user is authorized. */
@Composable
private fun AuthorizedScreen(
    accountInfo: String,
    onListChats: () -> Unit,
    onLogout: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterStart
    ) {
        Text(text = accountInfo, style = MaterialTheme.typrology.subtitle1)

        spacer(height = 16.dp)

        Button(
            onClick = onListChats,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
        ) {
            Text("List My Chats")
        }

        spacer(height = 16.dp)

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
        ) {
            Text("Logout")
        }
    }
}

/** Error state UI. */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.fillMaxWidth()
        )

        spacer(height = 8.dp)

        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Retry")
        }

        spacer(height = 8.dp)

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

/** ViewModel managing the auth state flow. */
class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotConfigured)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var userId: Long get() = 0
        private set

    /** Start TDLib initialization. */
    fun startInitialization() {
        _authState.value = AuthState.Initializing
        // TODO: Set up TDLib parameters and initialize client
        // For now, transition to waiting for phone number
        _authState.value = AuthState.WaitingForPhoneNumber
    }

    /** Verify phone number and transition to code input. */
    fun verifyPhoneNumber(phoneNumber: String) {
        _authState.value = AuthState.WaitingForCode
        userId = 0 // Will be set after authorization
    }

    /** Verify the code and transition to authorized or password screen. */
    fun verifyCode(code: String) {
        _authState.value = AuthState.WaitingForPassword
        // TODO: Check if 2FA is enabled; if not, go directly to authorized
    }

    /** Verify 2FA password and transition to authorized. */
    fun verifyPassword(password: String) {
        _authState.value = AuthState.Authorized
        // TODO: Actually authenticate with TDLib password
    }

    /** Log out from Telegram. */
    fun logout() {
        _authState.value = AuthState.LoggingOut
        // TODO: Call repository logout
    }

    /** Reset error state. */
    fun resetError() {
        _authState.value = when (_authState.value) {
            is AuthState.Error -> AuthState.NotConfigured
            else -> _authState.value
        }
    }
}