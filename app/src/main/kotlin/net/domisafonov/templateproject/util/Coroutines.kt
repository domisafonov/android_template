package net.domisafonov.templateproject.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T : Any?> flowOfNotNull(vararg elements: T?): Flow<T> = flow {
    elements.forEach { e -> e?.let { emit(it) } }
}
