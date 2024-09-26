@file:OptIn(FlowPreview::class)

package net.domisafonov.templateproject.mvi

import androidx.compose.animation.scaleIn
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

class MviComponentKtTest {

    private lateinit var scope: CoroutineScope

    @Before
    fun initScope() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @After
    fun closeScope() {
        scope.cancel()
    }

    // FIXME: use actorDispatcher to check that the started actions are finished

    @Test
    fun simpleEndToEnd() = runTest {
        val component = mviComponent<State, Wish, Action, Effect, SideEffect>(
            scope = backgroundScope,
            initialState = State(1),
            bootstrapper = listOf(Action.Plus(100)),
            wishToAction = ::wishToAction,
            actor = ::actor,
            reducer = ::reducer,
            postProcessor = { _, _, action, effect -> when {
                (action as? Action.Plus)?.amount == 100 && effect is Effect.Plus -> listOf(Action.Plus(1000))
                else -> emptyList()
            } },
        )

        component.sendWish(Wish.WishPlus1)
        component.state.filter { it.value == 1102 }.first()
    }

    @Test
    fun simpleEndToEndSideEffects() = runTest {
        TODO()
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
            testBody(scope)
        }
    }
}

private data class State(val value: Int)

private sealed interface Wish {
    data object WishNothing : Wish
    data object WishPlus1 : Wish
    data object WishPlus10 : Wish
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
}

private fun wishToAction(wish: Wish): List<Action> = when (wish) {
    Wish.WishNothing -> emptyList()
    Wish.WishPlus1 -> listOf(Action.Plus(1))
    Wish.WishPlus10 -> listOf(Action.Plus(10))
}

private fun actor(state: State, action: Action): Flow<Effect> = when (action) {
    is Action.Plus -> flowOf(Effect.Plus(amount = action.amount))
}

private fun reducer(state: State, effect: Effect): State = when (effect) {
    is Effect.NoEffect -> state
    is Effect.Plus -> state.copy(value = state.value + effect.amount)
}
