package net.domisafonov.templateproject.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.R

@Composable
fun TopBar(appState: AppState) {

    val defaultLabel = stringResource(id = R.string.default_label)

    val state by appState.navController.currentBackStackEntryFlow
        .map {
            TopBarState(
                title = it.destination.label?.toString() ?: defaultLabel,
                hasBackButton = it.destination.route != MAIN_NAV_ID,
            )
        }
        .collectAsState(
            initial = TopBarState(
                title = defaultLabel,
                hasBackButton = false,
            )
        )

    TopBarUi(
        state = state,
        onBackButtonClick = { appState.navController.popBackStack() },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBarUi(
    state: TopBarState,
    onBackButtonClick: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(state.title) },
        navigationIcon = {
            if (state.hasBackButton) {
                IconButton(onClick = onBackButtonClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button_description),
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun TopBarUiPreviewWithBack() {
    TopBarUi(state = TopBarState("Title", hasBackButton = false))
}

@Preview
@Composable
fun TopBarUiPreviewWithoutBack() {
    TopBarUi(state = TopBarState("Title", hasBackButton = true))
}

@Stable
private data class TopBarState(
    val title: String,
    val hasBackButton: Boolean,
)
