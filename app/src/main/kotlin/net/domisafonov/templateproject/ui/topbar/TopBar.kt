package net.domisafonov.templateproject.ui.topbar

import androidx.compose.foundation.layout.RowScope
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
import net.domisafonov.templateproject.ui.AppState
import net.domisafonov.templateproject.ui.MAIN_NAV_ID

@Composable
fun TopBar(appState: AppState) {

    val defaultLabel = stringResource(id = R.string.default_label)

    val actions: @Composable RowScope.() -> Unit = {
        appState.appBarActions.value?.invoke(this)
    }

    val state by appState.navController.currentBackStackEntryFlow
        .map {
            if (it.destination.navigatorName == "dialog") {
                appState.navController.previousBackStackEntry ?: it
            } else {
                it
            }
        }
        .map {
            TopBarState(
                title = it.destination.label?.toString() ?: defaultLabel,
                hasBackButton = it.destination.route != MAIN_NAV_ID,
                actions = actions,
            )
        }
        .collectAsState(
            initial = TopBarState(
                title = defaultLabel,
                hasBackButton = false,
                actions = {},
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
        },
        actions = state.actions,
    )
}

@Preview
@Composable
fun TopBarUiPreviewWithBack() {
    TopBarUi(state = TopBarState("Title", hasBackButton = false, actions = {}))
}

@Preview
@Composable
fun TopBarUiPreviewWithoutBack() {
    TopBarUi(state = TopBarState("Title", hasBackButton = true, actions = {}))
}

@Stable
private data class TopBarState(
    val title: String,
    val hasBackButton: Boolean,
    val actions: @Composable RowScope.() -> Unit,
)
