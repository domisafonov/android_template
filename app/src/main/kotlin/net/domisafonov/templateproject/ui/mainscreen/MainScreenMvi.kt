package net.domisafonov.templateproject.ui.mainscreen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import net.domisafonov.templateproject.mvi.MviComponent
import net.domisafonov.templateproject.mvi.mviComponent
import net.domisafonov.templateproject.util.flowOfNotNull

object MainScreenMvi {
    data class State(
        val isActivated: Boolean,
    )

    sealed interface Wish {
        data object GoButtonClick : Wish
        data object TenthClick : Wish
        data object WordCountClick : Wish
        data object UrlButtonClick : Wish
    }

    sealed interface Action {
        data object Activate : Action
        data object GoToTenth : Action
        data object GoToWordCount : Action
        data object GoToUrlEditor : Action
    }

    sealed interface Effect {
        data object Activated : Effect
        data object NoEffect : Effect
    }

    class Actor {
        fun execute(
            state: State,
            action: Action,
        ): Flow<Effect> = when(action) {
            is Action.Activate -> flowOfNotNull(Effect.Activated.takeIf { !state.isActivated })
            is Action.GoToTenth -> flowOf(Effect.NoEffect)
            is Action.GoToWordCount -> flowOf(Effect.NoEffect)
            is Action.GoToUrlEditor -> flowOf(Effect.NoEffect)
        }
    }

    private fun reduce(state: State, effect: Effect): State = when (effect) {
        is Effect.Activated -> state.copy(isActivated = true)
        is Effect.NoEffect -> state
    }

    sealed interface SideEffect
    sealed interface Command : SideEffect {
        data object UrlNotEditedMessage : Command
    }
    sealed interface Navigation : SideEffect {
        data object GoToTenth : Navigation
        data object GoToWordCount : Navigation
        data object GoToUrlEditor : Navigation
    }

    fun Component(
        scope: CoroutineScope,
        actor: Actor,
    ): MviComponent<State, Wish, SideEffect> = mviComponent(
        scope = scope,
        initialState = State(
            isActivated = false,
        ),
        wishToAction = { when (val wish = it) {
            is Wish.GoButtonClick -> listOf(Action.Activate)
            is Wish.TenthClick -> listOf(Action.GoToTenth)
            is Wish.WordCountClick -> listOf(Action.GoToWordCount)
            is Wish.UrlButtonClick -> listOf(Action.GoToUrlEditor)
        } },
        actor = actor::execute,
        reducer = ::reduce,
        sideEffectSource = { _, _, action, effect -> when {
            action is Action.GoToTenth && effect is Effect.NoEffect -> listOf(Navigation.GoToTenth)
            action is Action.GoToWordCount && effect is Effect.NoEffect -> listOf(Navigation.GoToWordCount)
            action is Action.GoToUrlEditor && effect is Effect.NoEffect -> listOf(Navigation.GoToUrlEditor)
            else -> emptyList()
        } },
    )
}
