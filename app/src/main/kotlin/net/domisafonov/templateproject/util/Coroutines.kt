package net.domisafonov.templateproject.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T : Any?> flowOfNotNull(vararg elements: T?): Flow<T> = flow {
    elements.forEach { e -> e?.let { emit(it) } }
}

fun <T> Flow<T>.flowWhenResumed(
    lifecycle: Lifecycle,
): Flow<T> = flowWithLifecycle(
    lifecycle = lifecycle,
    minActiveState = Lifecycle.State.RESUMED
)
