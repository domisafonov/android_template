@file:OptIn(ExperimentalCoroutinesApi::class)

package net.domisafonov.templateproject.mvi

import kotlinx.coroutines.CoroutineDispatcher
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.domisafonov.templateproject.BuildConfig
import timber.log.Timber

internal const val DEFAULT_WISH_CAPACITY = 64
internal const val DEFAULT_SIDE_EFFECT_BUFFER_CAPACITY = 64
internal const val DEFAULT_ACTION_CONCURRENCY_LIMIT = 16
internal fun defaultErrorHandler(e: Exception): Boolean = // TODO: non-android variation for KMP
    if (BuildConfig.DEBUG) {
        false
    } else {
        Timber.e(e, "error in mvi callback")
        true
    }

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

fun <State : Any, Wish : Any, Action : Any, Effect : Any, SideEffect : Any> mviComponent(
    scope: CoroutineScope,
    initialState: State,
    bootstrapper: List<Action> = emptyList(),
    wishToAction: (wish: Wish) -> List<Action>,
    actor: (state: State, action: Action) -> Flow<Effect>,
    reducer: (state: State, effect: Effect) -> State,
    postProcessor: PostProcessor<State, Action, Effect> = { _, _, _, _ -> emptyList() },
    sideEffectSource: SideEffectPublisher<State, Action, Effect, SideEffect> = { _, _, _, _ -> emptyList() },
    areSideEffectsSavedWithNoSubscribers: Boolean = false,
    wishCapacity: Int = DEFAULT_WISH_CAPACITY,
    actionConcurrencyLimit: Int = DEFAULT_ACTION_CONCURRENCY_LIMIT,
    sideEffectBufferCapacity: Int = DEFAULT_SIDE_EFFECT_BUFFER_CAPACITY,
    actorDispatcher: CoroutineDispatcher? = null,
    errorHandler: (e: Exception) -> Boolean = ::defaultErrorHandler,
): MviComponent<State, Wish, SideEffect> {
    if (wishCapacity <= 0) {
        throw IllegalArgumentException()
    }

    val input = Channel<Wish>(
        capacity = wishCapacity,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
        onUndeliveredElement = { Timber.e(
            if (BuildConfig.DEBUG) {
                "undelivered wish: $it"
            } else {
                "undelivered wish: ${it::class.qualifiedName}"
            }
        ) }
    )

    val state = MutableStateFlow(initialState)

    val sideEffects = Channel<SideEffect>(Channel.UNLIMITED)

    val component = object : MviComponent<State, Wish, SideEffect>, SendChannel<Wish> by input {
        private val _sideEffects: SharedFlow<SideEffect> = sideEffects.receiveAsFlow()
            .shareIn(
                scope = scope,
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

    scope.launch {
        val inducedActions = Channel<Action>(Channel.UNLIMITED)

        val allActions = merge(
            bootstrapper.asFlow(),
            input.consumeAsFlow()
                .mapNotNull { wrap(errorHandler) { wishToAction(it) }?.asFlow() }
                .flattenConcat(),
            inducedActions.consumeAsFlow(),
        )

        allActions
            .mapNotNull { action ->
                wrap(errorHandler) {
                    if (actorDispatcher != null) {
                        withContext(actorDispatcher) {
                            actor(state.value, action)
                        }
                    } else {
                        actor(state.value, action)
                    }
                }
                    ?.catch { e ->
                        e as? Exception ?: throw e
                        if (errorHandler(e)) {
                            emptyFlow<Effect>()
                        } else {
                            throw e
                        }
                    }
                    ?.map { action to it }
            }
            .flattenMerge(concurrency = actionConcurrencyLimit)
            .collect { (action, effect) ->
                val oldState = state.value
                val newState = wrap(errorHandler) { reducer(oldState, effect) } ?: return@collect
                state.value = newState
                wrap(errorHandler) { postProcessor(oldState, newState, action, effect) }
                    ?.forEach { inducedActions.trySend(it) }
                wrap(errorHandler) { sideEffectSource(oldState, newState, action, effect) }
                    ?.forEach { sideEffects.trySend(it) }
            }
    }

    return component
}

private val ResettingReplayCacheOnZeroImpl = SharingStarted { subscriptionCount -> flow {
    emit(SharingCommand.START)
    var isFirst = true

    subscriptionCount.collect { count ->
        if (isFirst) {
            isFirst = false
            return@collect
        }
        if (count == 0) {
            emit(SharingCommand.STOP_AND_RESET_REPLAY_CACHE)
            emit(SharingCommand.START)
        }
    }
} }

private val SharingStarted.Companion.ResettingReplayCacheOnZero get() = ResettingReplayCacheOnZeroImpl

private inline fun <R : Any> wrap(
    handler: (Exception) -> Boolean,
    lambda: () -> R
): R? = try {
    lambda()
} catch (e: Exception) {
    if (handler(e)) {
        null
    } else {
        throw e
    }
}
