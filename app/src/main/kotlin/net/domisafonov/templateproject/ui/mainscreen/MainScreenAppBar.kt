package net.domisafonov.templateproject.ui.mainscreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import net.domisafonov.templateproject.R

@Composable
fun MainScreenAppBar(
    state: State<MainScreenAppBarState?>,
    events: SendChannel<MainScreenAppBarEvent>,
) {
    val state = state.value ?: return

    if (!state.isActivated) {
        IconButton(
            onClick = { events.trySend(MainScreenAppBarEvent.UrlButtonClick) }
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(id = R.string.url_description),
            )
        }
    }
}

@Immutable
data class MainScreenAppBarState(
    val isActivated: Boolean,
)

sealed interface MainScreenAppBarEvent {
    data object UrlButtonClick : MainScreenAppBarEvent
}

@Preview
@Composable
fun MainScreenAppBarPreview() {
    MainScreenAppBar(
        state = remember { mutableStateOf(MainScreenAppBarState(isActivated = false)) },
        events = Channel(),
    )
}
