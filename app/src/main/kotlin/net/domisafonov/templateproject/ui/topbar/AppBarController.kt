package net.domisafonov.templateproject.ui.topbar

import androidx.compose.runtime.Stable
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow

@Stable
class AppBarController<State : Any, Event : Any>(
    private val state: SendChannel<State>,
    val events: Flow<Event>,
) {
    fun setState(state: State) {
        this.state.trySend(state)
    }
}
