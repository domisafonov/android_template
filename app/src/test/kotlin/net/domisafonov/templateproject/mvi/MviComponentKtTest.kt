package net.domisafonov.templateproject.mvi

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

class MviComponentKtTest {

    private lateinit var multithreadedScope: CoroutineScope

    @Before
    fun initScope() {
        multithreadedScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @After
    fun closeScope() {
        multithreadedScope.cancel()
    }

    // FIXME: use actorDispatcher to check that the started actions are finished

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
        component.sendWish(Wish.WishPlus1)
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
        component.sendWish(Wish.WishPlus200)
        testScheduler.advanceUntilIdle()
        assertThat(component.sideEffects.take(3).toList())
            .containsExactly(SideEffect.SideEffect1, SideEffect.SideEffect2, SideEffect.SideEffect2)
        component.state.filter { it.value == 501 }.first()
    }

    // 1. end to end state is emitted, changed, a side effect is triggered
    // 2. each specific callback is getting called
    // 3. empty values at each place
    // 4. multiple values at each place
    // 5. ongoing action
    // 6. parallel actions (emitting periodically)
    // 7. recursion with postProcessor, should also test that no action is getting lost
    // 8. the limits
    // 9. errors thrown in each callback
    // 10. empty values from callbacks do not trigger other callbacks
    // 11. the subscriber count thing
    // 12. all arguments in each callback

    private fun runTest(
        context: CoroutineContext = EmptyCoroutineContext,
        testBody: suspend TestScope.(mviScope: CoroutineScope) -> Unit,
    ) {
        // backgroundScope is single-threaded
        kotlinx.coroutines.test.runTest(
            context = context,
            timeout = 100.milliseconds,
        ) {
            testBody(backgroundScope)
        }

        // scope is multithreaded
        kotlinx.coroutines.test.runTest(
            context = context,
            timeout = 100.milliseconds,
        ) {
            testBody(multithreadedScope)
        }
    }
}

private data class State(val value: Int)

private sealed interface Wish {
    data object WishNothing : Wish
    data object WishPlus1 : Wish
    data object WishPlus10 : Wish
    data object WishPlus200 : Wish
}

private sealed interface Action {
    data class Plus(val amount: Int) : Action
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
    Wish.WishNothing -> emptyList()
    Wish.WishPlus1 -> listOf(Action.Plus(1))
    Wish.WishPlus10 -> listOf(Action.Plus(10))
    Wish.WishPlus200 -> listOf(Action.Plus(200))
}

private fun actor(state: State, action: Action): Flow<Effect> = when (action) {
    is Action.Plus -> flowOf(Effect.Plus(amount = action.amount))
}

private fun reducer(state: State, effect: Effect): State = when (effect) {
    is Effect.NoEffect -> state
    is Effect.Plus -> state.copy(value = state.value + effect.amount)
}
