package net.domisafonov.templateproject.ui.mainscreen

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun MainScreenAppBar(
    state: Flow<MainScreenAppBarState>,
    events: SendChannel<Unit>,
) {
    val state = state.collectAsState(initial = null).value ?: return

    // TODO
    if (state.isActivated) {
        Text("aaa", Modifier.clickable { events.trySend(Unit) })
    } else {
        Text("bbb")
    }
}

@Immutable
data class MainScreenAppBarState(
    val isActivated: Boolean,
)

@Preview
@Composable
fun MainScreenAppBarPreview() {
    MainScreenAppBar(
        state = flowOf(MainScreenAppBarState(isActivated = true)),
        events = Channel(),
    )
}
