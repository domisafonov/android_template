@file:OptIn(ExperimentalCoroutinesApi::class)

package net.domisafonov.templateproject.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import net.domisafonov.templateproject.BuildConfig
import timber.log.Timber

private const val DEFAULT_WISH_CAPACITY = 64
private const val DEFAULT_SIDE_EFFECT_BUFFER_CAPACITY = 64
private const val DEFAULT_ACTION_CONCURRENCY_LIMIT = 16

interface MviComponent<State : Any, Wish : Any, SideEffect : Any> : SendChannel<Wish> {
    val state: StateFlow<State>
    val sideEffects: SharedFlow<SideEffect>
}

fun <State : Any, Wish : Any, SideEffect : Any> MviComponent<State, Wish, SideEffect>.sendWish(wish: Wish) {
    trySend(wish)
}

typealias PostProcessor<State, Action, Effect> = (
    oldState: State,
    newState: State,
    action: Action,
    effect: Effect,
) -> List<Action>

typealias SideEffectPublisher<State, Action, Effect, SideEffect> = (
    oldState: State,
    newState: State,
    action: Action,
    effect: Effect,
) -> List<SideEffect>

fun <State : Any, Wish : Any, Action : Any, Effect : Any, SideEffect : Any> CoroutineScope.mviComponent(
    initialState: State,
    bootstrapper: List<Action> = emptyList(),
    wishToAction: (wish: Wish) -> List<Action>,
    actor: (state: State, action: Action) -> Flow<Effect>,
    reducer: (state: State, effect: Effect) -> State,
    postProcessor: PostProcessor<State, Action, Effect> = { _, _, _, _ -> emptyList() },
    sideEffectSource: SideEffectPublisher<State, Action, Effect, SideEffect> = { _, _, _, _ -> emptyList() },
    areSideEffectsSavedWithNoSubscribers: Boolean = true,
    wishCapacity: Int = DEFAULT_WISH_CAPACITY,
    actionConcurrencyLimit: Int = DEFAULT_ACTION_CONCURRENCY_LIMIT,
    sideEffectBufferCapacity: Int = DEFAULT_SIDE_EFFECT_BUFFER_CAPACITY,
): MviComponent<State, Wish, SideEffect> {
    if (wishCapacity <= 0) {
        throw IllegalArgumentException()
    }

    val input = Channel<Wish>(
        capacity = wishCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        onUndeliveredElement = { Timber.e(
            if (BuildConfig.DEBUG) {
                "undelivered wish: $it"
            } else {
                "undelivered wish: ${it::class.qualifiedName}"
            }
        ) }
    )

    val state = MutableStateFlow(initialState)

    val sideEffects = Channel<SideEffect>()

    val component = object : MviComponent<State, Wish, SideEffect>, SendChannel<Wish> by input {
        private val _sideEffects: SharedFlow<SideEffect> = sideEffects.receiveAsFlow()
            .shareIn(
                scope = this@mviComponent,
                started = if (areSideEffectsSavedWithNoSubscribers) {
                    SharingStarted.Eagerly
                } else {
                    SharingStarted.ResettingReplayCacheOnZero
                },
                replay = sideEffectBufferCapacity,
            )

        override val state: StateFlow<State> = state.asStateFlow()

        override val sideEffects: SharedFlow<SideEffect> = _sideEffects
    }

    launch {

        val inducedActions = Channel<Action>(Channel.UNLIMITED)

        val allActions = merge(
            bootstrapper.asFlow(),
            input.consumeAsFlow().flatMapConcat { wishToAction(it).asFlow() },
            inducedActions.consumeAsFlow(),
        )

        allActions
            .flatMapMerge(concurrency = actionConcurrencyLimit) { action ->
                actor(state.value, action).map { action to it }
            }
            .collect { (action, effect) ->
                val oldState = state.value
                val newState = reducer(oldState, effect)
                state.value = newState
                postProcessor(oldState, newState, action, effect).forEach { inducedActions.send(it) }
                sideEffectSource(oldState, newState, action, effect).forEach { sideEffects.trySend(it) }
            }
    }

    return component
}

private val ResettingReplayCacheOnZeroImpl = SharingStarted { subscriptionCount ->
    subscriptionCount.flatMapConcat { count ->
        if (count == 0) {
            flowOf(SharingCommand.STOP_AND_RESET_REPLAY_CACHE, SharingCommand.START)
        } else {
            emptyFlow()
        }
    }
}

private val SharingStarted.Companion.ResettingReplayCacheOnZero get() = ResettingReplayCacheOnZeroImpl
