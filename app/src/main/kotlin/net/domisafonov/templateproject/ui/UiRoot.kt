package net.domisafonov.templateproject.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.domisafonov.templateproject.R
import net.domisafonov.templateproject.ui.theme.TemplateProjectTheme
import net.domisafonov.templateproject.ui.topbar.TopBar

@Composable
fun UiRoot(
    appState: AppState,
    modifier: Modifier = Modifier,
) {
    TemplateProjectTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = { TopBar(appState) },
            snackbarHost = { SnackbarHost(hostState = appState.snackbarHostState) },
        ) { innerPadding ->
            NavHost(
                appState = appState,
                modifier = Modifier.padding(innerPadding),
            )

            IsOfflineMessages(
                isOffline = appState.isOffline.value,
                snackbarHostState = appState.snackbarHostState,
            )
        }
    }
}

@Composable
private fun IsOfflineMessages(
    isOffline: Boolean?,
    snackbarHostState: SnackbarHostState,
) {
    isOffline ?: return

    var isFirstEvent by remember { mutableStateOf(true) }

    val isOfflineMessage = stringResource(id = R.string.is_offline_message)
    val isOnlineMessage = stringResource(id = R.string.is_online_message)

    LaunchedEffect (isOffline) {
        if (isFirstEvent) {
            isFirstEvent = false
            if (!isOffline) { return@LaunchedEffect }
        }

        snackbarHostState.showSnackbar(
            if (isOffline) { isOfflineMessage } else { isOnlineMessage }
        )
    }
}
