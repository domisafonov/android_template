@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package net.domisafonov.templateproject.mvi

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.AssertionError
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

class MviComponentKtTest {
    @Test
    fun simpleEndToEnd() = runTest { scope ->
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(1000))
                (action as? Action.Plus)?.amount == 1000 && effect is Effect.Plus -> listOf(Action.Plus(10000))
                else -> emptyList()
            } },
        )
        component.sendWish(Wish.Plus1)
        component.state.filter { it.value == 11102 }.first()
    }

    @Test
    fun simpleEndToEndSideEffects() = runTest { scope ->
        val component = mviComponent(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(200))
                else -> emptyList()
            } },
            sideEffectSource = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(SideEffect.SideEffect1)
                (action as? Action.Plus)?.amount == 200 && effect is Effect.Plus -> listOf(SideEffect.SideEffect2)
                else -> emptyList()
            } },
        )
        component.sendWish(Wish.Plus200)
        assertThat(component.sideEffects.take(3).toList())
            .containsExactly(SideEffect.SideEffect1, SideEffect.SideEffect2, SideEffect.SideEffect2)
        component.state.filter { it.value == 501 }.first()
    }

    @Test
    fun wishToActionIsCalled() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = { callCount.getAndIncrement(); wishToAction(it) },
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(1000))
                else -> emptyList()
            } },
        )
        component.sendWish(Wish.Plus10)
        component.state.filter { it.value == 1111 }.first()
        assertThat(callCount.get()).isEqualTo(1)
    }

    @Test
    fun actorIsCalled() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = { state, action -> callCount.getAndIncrement(); actor(state, action) },
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(1000))
                else -> emptyList()
            } },
        )
        component.sendWish(Wish.Plus10)
        component.state.filter { it.value == 1111 }.first()
        assertThat(callCount.get()).isEqualTo(3)
    }

    @Test
    fun reducerIsCalled() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = { state, effect -> callCount.getAndIncrement(); reducer(state, effect) },
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(1000))
                else -> emptyList()
            } },
        )
        component.sendWish(Wish.Plus10)
        component.state.filter { it.value == 1111 }.first()
        assertThat(callCount.get()).isEqualTo(3)
    }

    @Test
    fun postProcessorIsCalled() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> callCount.getAndIncrement(); when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(1000))
                else -> emptyList()
            } },
        )
        component.sendWish(Wish.Plus10)
        component.state.filter { it.value == 1111 }.first()
        assertThat(callCount.get()).isEqualTo(3)
    }

    @Test
    fun sideEffectProducerIsCalled() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(1000))
                else -> emptyList()
            } },
            sideEffectSource = { _, _, _, _ -> callCount.getAndIncrement(); emptyList() },
        )
        component.sendWish(Wish.Plus10)
        component.state.filter { it.value == 1111 }.first()
        assertThat(callCount.get()).isEqualTo(3)
    }

    @Test
    fun returnEmptyFromWishToAction() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            wishToAction = ::wishToAction,
            actor = { state, action -> callCount.getAndIncrement(); actor(state, action) },
            reducer = ::reducer,
        )
        component.sendWish(Wish.Nothing)
        component.sendWish(Wish.Plus10)
        component.state.filter { it.value == 11 }.first()
        assertThat(callCount.get()).isEqualTo(1)
    }

    @Test
    fun returnEmptyFromActor() = runTest { scope -> // normal, from postprocessor
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Empty),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = { state, effect -> callCount.getAndIncrement(); reducer(state, effect) },
            actionConcurrencyLimit = 1,
        )
        component.sendWish(Wish.Empty)
        component.sendWish(Wish.Plus10)
        component.state.filter { it.value == 11 }.first()
        assertThat(callCount.get()).isEqualTo(1)
    }

    @Test
    fun returnUnchangedFromReducer() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Ineffective),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = { state, effect -> callCount.getAndIncrement(); reducer(state, effect) },
            actionConcurrencyLimit = 1,
        )
        component.sendWish(Wish.Ineffective)
        component.sendWish(Wish.Plus10)
        val states = component.state.take(2).toList()
        assertThat(states.last().value).isEqualTo(11)
        if (states.size == 2 && states.first().value != 1) {
            throw AssertionError("weird intermediate state: ${states.first()}")
        }
    }

    @Test
    fun returnMultipleFromWishToAction() = runTest { scope ->
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = { state, effect -> callCount.getAndIncrement(); reducer(state, effect) },
            actionConcurrencyLimit = 1,
        )
        component.sendWish(Wish.Multiplus100)
        component.state.filter { it.value == 101 }.first()
    }

    @Test
    fun returnMultipleFromActor() = runTest { scope -> // normal, from postprocessor
        val callCount = AtomicInteger(0)
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = { state, effect -> callCount.getAndIncrement(); reducer(state, effect) },
            actionConcurrencyLimit = 1,
        )
        component.sendWish(Wish.ActorMultiplus100)
        component.state.filter { it.value == 101 }.first()
    }

    @Test
    fun returnMultipleFromPostProcessor() = runTest { scope ->
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus ->
                    listOf(Action.Plus(500), Action.Plus(500))
                else -> emptyList()
            } },
        )
        component.state.filter { it.value == 1101 }.first()
    }

    @Test
    fun returnMultipleFromSideEffectProducer() = runTest { scope ->
        val component = mviComponent(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            sideEffectSource = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus ->
                    listOf(SideEffect.SideEffect1, SideEffect.SideEffect1)
                else -> emptyList()
            } },
        )
        assertThat(component.sideEffects.take(2).toList())
            .containsExactly(SideEffect.SideEffect1, SideEffect.SideEffect1)
        component.state.filter { it.value == 101 }.first()
    }

    @Test
    fun parallelActions() = runTest { scope ->
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.LongMulti),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, _ -> when {
                (action as? Action.Plus)?.amount == 1 -> listOf(Action.Plus(2))
                else -> emptyList()
            } },
        )
        component.sendWish(Wish.ActorMultiplus100)
        scope.async { delay(11.milliseconds) }.await()
        component.sendWish(Wish.Plus1)
        scope.async { delay(5.milliseconds) }.await()
        component.sendWish(Wish.Plus1)
        scope.async { delay(10.milliseconds) }.await()
        component.sendWish(Wish.Plus1)
        component.state.filter { it.value == 207 }.first()
    }

    @Test
    fun componentExecutesOnItsScope() = runTest(doMultiScope = false) { scopeWithNamedThread { scope, name ->
        val wrongThread = AtomicReference(null as String?)

        fun checkDispatcher() {
            val threadName = Thread.currentThread().name
            if (!threadName.contains(name)) {
                wrongThread.set(threadName)
            }
        }

        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = { wish -> checkDispatcher(); wishToAction(wish) },
            actor = { state, action -> checkDispatcher(); actor(state, action) },
            reducer = { state, effect -> checkDispatcher(); reducer(state, effect) },
            postProcessor = { _, _, action, effect ->
                checkDispatcher()
                when {
                    (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(
                        Action.Plus(200)
                    )

                    else -> emptyList()
                }
            },
            sideEffectSource = { _, _, action, effect ->
                checkDispatcher()
                when {
                    (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(
                        SideEffect.SideEffect1
                    )

                    (action as? Action.Plus)?.amount == 200 && effect is Effect.Plus -> listOf(
                        SideEffect.SideEffect2
                    )

                    else -> emptyList()
                }
            },
        )
        component.sendWish(Wish.Plus200)
        assertThat(component.sideEffects.take(3).toList())
            .containsExactly(
                SideEffect.SideEffect1,
                SideEffect.SideEffect2,
                SideEffect.SideEffect2
            )
        component.state.filter { it.value == 501 }.first()

        assertThat(wrongThread.get()).isNull()
    } }

    @Test
    fun actorExecutesOnActorDispatcher() = runTest { scope ->
        TODO()
    }

    @Test
    fun postProcessorRecursion() = runTest { scope ->
        TODO()
    }

    @Test
    fun limits() = runTest { scope ->
        TODO()
    }

    @Test
    fun throwErrorFromWishToAction() = runTest { scope -> // bootstrapper, send
        TODO()
    }

    @Test
    fun throwErrorFromActor() = runTest { scope -> // normal, from postprocessor
        TODO()
    }

    @Test
    fun throwErrorFromReducer() = runTest { scope ->
        TODO()
    }

    @Test
    fun throwErrorFromPostProcessor() = runTest { scope ->
        TODO()
    }

    @Test
    fun throwErrorFromSideEffectProducer() = runTest { scope ->
        TODO()
    }

    @Test
    fun sideEffectWithNoSubscribersSavesValues() = runTest { scope ->
        TODO()
    }

    @Test
    fun sideEffectWithNoSubscribersDoesNotSaveValues() = runTest { scope ->
        TODO() // incl. save values on start, but not after reaching 0 subs later
    }

    @Test
    fun actorGetsConsistentState() = runTest { scope ->
        TODO()
    }

    @Test
    fun postProcessorGetsConsistentOldNewState() = runTest { scope ->
        TODO()
    }

    @Test
    fun sideEffectProducerGetsConsistentOldNewState() = runTest { scope ->
        TODO()
    }

    private fun runTest(
        context: CoroutineContext = EmptyCoroutineContext,
        doMultiScope: Boolean = true,
        testBody: suspend TestScope.(mviScope: CoroutineScope) -> Unit,
    ) {
        // backgroundScope is single-threaded
        kotlinx.coroutines.test.runTest(
            context = context,
            timeout = 100.milliseconds,
        ) {
            testBody(backgroundScope)
        }

        if (doMultiScope) {
            // scope is multithreaded
            kotlinx.coroutines.test.runTest(
                context = context,
                timeout = 100.milliseconds,
            ) {
                testBody(multithreadedScope)
            }
        }
    }

    private lateinit var multithreadedScope: CoroutineScope

    @Before
    fun initScope() {
        multithreadedScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @After
    fun closeScope() {
        multithreadedScope.cancel()
    }
}

private data class State(val value: Int)

private sealed interface Wish {
    data object Nothing : Wish
    data object Empty : Wish
    data object Ineffective : Wish
    data object Plus1 : Wish
    data object Plus10 : Wish
    data object Plus200 : Wish
    data object Multiplus100 : Wish
    data object ActorMultiplus100: Wish
}

private sealed interface Action {
    data class Plus(val amount: Int) : Action
    data class DoublePlus(val amount: Int) : Action
    data object Empty : Action
    data object Ineffective : Action
    data object LongMulti : Action
}

private sealed interface Effect {
    data object NoEffect : Effect
    data class Plus(val amount: Int) : Effect
}

private sealed interface SideEffect {
    data object SideEffect1 : SideEffect
    data object SideEffect2 : SideEffect
}

private fun wishToAction(wish: Wish): List<Action> = when (wish) {
    is Wish.Nothing -> emptyList()
    is Wish.Empty -> listOf(Action.Empty)
    is Wish.Ineffective -> listOf(Action.Ineffective)
    is Wish.Plus1 -> listOf(Action.Plus(1))
    is Wish.Plus10 -> listOf(Action.Plus(10))
    is Wish.Plus200 -> listOf(Action.Plus(200))
    is Wish.Multiplus100 -> listOf(Action.Plus(50), Action.Plus(50))
    is Wish.ActorMultiplus100 -> listOf(Action.DoublePlus(50))
}

private fun actor(@Suppress("UNUSED_PARAMETER") state: State, action: Action): Flow<Effect> = when (action) {
    is Action.Plus -> flowOf(Effect.Plus(amount = action.amount))
    is Action.DoublePlus -> flowOf(Effect.Plus(amount = action.amount), Effect.Plus(amount = action.amount))
    is Action.Empty -> emptyFlow()
    is Action.Ineffective -> flowOf(Effect.NoEffect)
    is Action.LongMulti -> flow {
        for (i in 1..10) {
            emit(Effect.Plus(10))
            delay(1.milliseconds)
        }
    }
}

private fun reducer(state: State, effect: Effect): State = when (effect) {
    is Effect.NoEffect -> state
    is Effect.Plus -> state.copy(value = state.value + effect.amount)
}

private suspend fun scopeWithNamedThread(lambda: suspend (scope: CoroutineScope, name: String) -> Unit) {
    val name = UUID.randomUUID().toString()
    newSingleThreadContext(name).use { dispatcher ->
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        try {
            lambda(scope, name)
        } finally {
            scope.coroutineContext.cancel()
        }
    }
}
