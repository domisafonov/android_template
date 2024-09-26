@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package net.domisafonov.templateproject.mvi

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.yield
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
            sideEffectSource = { _, _, action, _ ->
                if ((action as? Action.Plus)?.amount != 1) {
                    callCount.getAndIncrement()
                }
                emptyList()
            },
        )
        component.sendWish(Wish.Plus10)
        component.sendWish(Wish.Plus1)
        component.state.filter { it.value == 1112 }.first()
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
    fun returnEmptyFromActor() = runTest { scope ->
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

    // FIXME: also, test that it actually runs in parallel
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
        scope.launch { delay(11.milliseconds) }.join()
        component.sendWish(Wish.Plus1)
        scope.launch { delay(5.milliseconds) }.join()
        component.sendWish(Wish.Plus1)
        scope.launch { delay(10.milliseconds) }.join()
        component.sendWish(Wish.Plus1)
        component.state.filter { it.value == 207 }.first()
    }

    @Test
    fun componentExecutesOnItsScope() = runTest(doBackgroundOnly = true) { scopeWithNamedThread { scope, name ->
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
                    (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(200))
                    else -> emptyList()
                }
            },
            sideEffectSource = { _, _, action, effect ->
                checkDispatcher()
                when {
                    (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(SideEffect.SideEffect1)
                    (action as? Action.Plus)?.amount == 200 && effect is Effect.Plus -> listOf(SideEffect.SideEffect2)
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
    fun actorExecutesOnActorDispatcher() = runTest { scope -> newSingleThreadContext("actorExecutesOnActorDispatcher")
        .use { singleThread ->
            val wrongThread = AtomicReference(null as String?)

            fun checkDispatcher(isActorThread: Boolean = false) {
                val threadName = Thread.currentThread().name
                if (isActorThread xor threadName.contains("actorExecutesOnActorDispatcher")) {
                    wrongThread.set(threadName)
                }
            }

            val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
                scope = scope,
                initialState = State(1),
                bootstrapper = listOf(Action.Plus(100)),
                wishToAction = { wish -> checkDispatcher(); wishToAction(wish) },
                actor = { state, action -> checkDispatcher(isActorThread = true); actor(state, action) },
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
                        (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus ->
                            listOf(SideEffect.SideEffect1)
                        (action as? Action.Plus)?.amount == 200 && effect is Effect.Plus ->
                            listOf(SideEffect.SideEffect2)
                        else -> emptyList()
                    }
                },
                actorDispatcher = singleThread,
            )
            component.sendWish(Wish.Plus200)
            assertThat(component.sideEffects.take(3).toList())
                .containsExactly(
                    SideEffect.SideEffect1,
                    SideEffect.SideEffect2,
                    SideEffect.SideEffect2,
                ).inOrder()
            component.state.filter { it.value == 501 }.first()

            assertThat(wrongThread.get()).isNull()
        }
    }

    @Test
    fun postProcessorRecursion() = runTest { scope ->
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = scope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(1)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                action is Action.Plus && action.amount < 5 && effect is Effect.Plus ->
                    listOf(Action.Plus(action.amount + 1), Action.Plus(amount = action.amount + 1))
                else -> emptyList()
            } },
        )
        component.state.filter { it.value == 130 }.first()
    }

    @Test
    fun throwErrorFromWishToActionAndContinue() = runTest { scope ->
        val (component, errors) = makeWishErrorComponent(scope = scope)
        component.sendWish(Wish.Plus10)
        component.sendWish(Wish.Plus200)
        component.state.filter { it.value == 201 }.first()
        errors.close()
        assertThat(errors.consumeAsFlow().toList().map { it::class }).containsExactly(Exception::class)
    }

    @Test(expected = UniqueError::class)
    fun throwErrorFromWishToActionStopByErrorBackground() = runTest(doBackgroundOnly = true) { scope ->
        val (component, _) = makeWishErrorComponent(scope = scope)
        component.sendWish(Wish.Plus1)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueError::class)
    fun throwErrorFromWishToActionStopByErrorMulti() = runTest(doMultiOnly = true) { scope ->
        val (component, _) = makeWishErrorComponent(scope = scope)
        component.sendWish(Wish.Plus1)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueException::class)
    fun throwErrorFromWishToActionStopByHandlerBackground() = runTest(doBackgroundOnly = true) { scope ->
        val (component, _) = makeWishErrorComponent(scope = scope)
        component.sendWish(Wish.Empty)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueException::class)
    fun throwErrorFromWishToActionStopByHandlerMulti() = runTest(doMultiOnly = true) { scope ->
        val (component, _) = makeWishErrorComponent(scope = scope)
        component.sendWish(Wish.Empty)
        waitForComponentToTerminate(scope)
    }

    @Test
    fun throwErrorFromActorAndContinue() = runTest { scope ->
        val (component, errors) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus200)
        component.sendWish(Wish.Plus2)
        component.sendWish(Wish.Plus300)
        component.sendWish(Wish.Plus3)
        component.state.filter { it.value == 9 }.first()
        errors.close()
        assertThat(errors.consumeAsFlow().toList().map { it::class })
            .containsExactly(Exception::class, Exception::class, Exception::class)
    }

    @Test(expected = UniqueError::class)
    fun throwErrorFromActorStopByErrorInsideFlowBackground() = runTest(doBackgroundOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus10)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueError::class)
    fun throwErrorFromActorStopByErrorInsideFlowMulti() = runTest(doMultiOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus10)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueError::class)
    fun throwErrorFromActorStopByErrorOutsideFlowBackground() = runTest(doBackgroundOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus201)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueError::class)
    fun throwErrorFromActorStopByErrorOutsideFlowMulti() = runTest(doMultiOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus201)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueException::class)
    fun throwErrorFromActorStopByHandlerInsideFlowBackground() = runTest(doBackgroundOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus1)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueException::class)
    fun throwErrorFromActorStopByHandlerInsideFlowMulti() = runTest(doMultiOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus1)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueException::class)
    fun throwErrorFromActorStopByHandlerOutsideFlowBackground() = runTest(doBackgroundOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus11)
        waitForComponentToTerminate(scope)
    }

    @Test(expected = UniqueException::class)
    fun throwErrorFromActorStopByHandlerOutsideFlowMulti() = runTest(doMultiOnly = true) { scope ->
        val (component, _) = makeActorErrorComponent(scope = scope)
        component.sendWish(Wish.Plus11)
        waitForComponentToTerminate(scope)
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

    @Test
    fun limits() = runTest { scope ->
        TODO()
    }

    private fun runTest(
        context: CoroutineContext = EmptyCoroutineContext,
        doBackgroundOnly: Boolean = false,
        doMultiOnly: Boolean = false,
        testBody: suspend TestScope.(mviScope: CoroutineScope) -> Unit,
    ) {
        if (doMultiOnly && doBackgroundOnly) {
            throw TestImplError()
        }

        if (!doMultiOnly) {
            // backgroundScope is single-threaded
            kotlinx.coroutines.test.runTest(
                context = context,
                timeout = 200.milliseconds,
            ) {
                testBody(backgroundScope)
            }
        }

        if (!doBackgroundOnly) {
            // scope is multithreaded
            kotlinx.coroutines.test.runTest(
                context = context,
                timeout = 200.milliseconds,
            ) {
                testBody(multithreadedScope)
                scopeError.error?.let {
                    scopeError.error = null
                    throw it
                }
            }
        }
    }

    private suspend fun waitForComponentToTerminate(scope: CoroutineScope) {
        // the one remaining job is side effects' shareIn
        while (scope.coroutineContext.job.children.count() > 1) {
            yield()
        }
    }

    private lateinit var multithreadedScope: CoroutineScope
    private lateinit var scopeError: ErrorStore

    @Before
    fun initScope() {
        scopeError = ErrorStore()
        multithreadedScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + scopeError)
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
    data object Plus2 : Wish
    data object Plus3 : Wish
    data object Plus10 : Wish
    data object Plus11 : Wish
    data object Plus200 : Wish
    data object Plus201 : Wish
    data object Plus300 : Wish
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
    is Wish.Plus2 -> listOf(Action.Plus(2))
    is Wish.Plus3 -> listOf(Action.Plus(3))
    is Wish.Plus10 -> listOf(Action.Plus(10))
    is Wish.Plus11 -> listOf(Action.Plus(11))
    is Wish.Plus200 -> listOf(Action.Plus(200))
    is Wish.Plus201 -> listOf(Action.Plus(201))
    is Wish.Plus300 -> listOf(Action.Plus(300))
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

private fun makeWishErrorComponent(
    scope: CoroutineScope,
): Pair<MviComponent<State, Wish, SideEffect>, Channel<java.lang.Exception>> {
    val errors = Channel<java.lang.Exception>(Channel.BUFFERED)

    return mviComponent<State, Wish, Action, Effect, SideEffect>(
        scope = scope,
        initialState = State(1),
        wishToAction = {
            when (it) {
                is Wish.Plus1 -> throw UniqueError()
                is Wish.Plus10 -> throw Exception()
                is Wish.Plus200 -> listOf(Action.Plus(200))
                else -> throw UniqueException()
            }
        },
        actor = ::actor,
        reducer = ::reducer,
        errorHandler = { e ->
            errors.trySend(e)
            e !is UniqueException
        },
    ) to errors
}

private fun makeActorErrorComponent(
    scope: CoroutineScope,
): Pair<MviComponent<State, Wish, SideEffect>, Channel<Exception>> {
    val errors = Channel<java.lang.Exception>(Channel.BUFFERED)

    return mviComponent<State, Wish, Action, Effect, SideEffect>(
        scope = scope,
        initialState = State(1),
        wishToAction = ::wishToAction,
        actor = { _, action -> when (action) {
            is Action.Plus -> {
                when (val amount = action.amount) {
                    1 -> flow { throw UniqueException() }
                    10 -> flow { throw UniqueError() }
                    200 -> flow { throw Exception() }
                    2 -> throw Exception()
                    11 -> throw UniqueException()
                    201 -> throw UniqueError()
                    300 -> flow { emit(Effect.Plus(5)); throw Exception() }
                    else -> flowOf(Effect.Plus(amount))
                }
            }
            else -> throw NotImplementedError()
        } },
        reducer = ::reducer,
        errorHandler = { e ->
            errors.trySend(e)
            e !is UniqueException
        },
    ) to errors
}

private suspend fun scopeWithNamedThread(lambda: suspend (scope: CoroutineScope, name: String) -> Unit) {
    val name = UUID.randomUUID().toString()
    newSingleThreadContext(name).use { dispatcher ->
        val scope = CoroutineScope(Job() + dispatcher)
        try {
            lambda(scope, name)
        } finally {
            scope.cancel()
        }
    }
}

private class ErrorStore : CoroutineExceptionHandler {
    @Volatile var error: Throwable? = null
    override val key = CoroutineExceptionHandler
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (error == null) {
            error = exception
        }
    }
}

private class UniqueError : Error()
private class UniqueException : Exception()
private class TestImplError : Error()
