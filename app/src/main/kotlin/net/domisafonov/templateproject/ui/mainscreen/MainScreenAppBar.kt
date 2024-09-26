package net.domisafonov.templateproject.ui.mainscreen

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

@Composable
fun MainScreenAppBar(
    state: State<MainScreenAppBarState?>,
    events: SendChannel<Unit>,
) {
    val state = state.value ?: return

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
        state = remember { mutableStateOf(MainScreenAppBarState(isActivated = true)) },
        events = Channel(),
    )
}
