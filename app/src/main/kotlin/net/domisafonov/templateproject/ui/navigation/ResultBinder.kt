package net.domisafonov.templateproject.ui.navigation

import android.os.Parcelable
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import net.domisafonov.templateproject.BuildConfig

interface ResultBinder<T : Parcelable> {
    val results: Flow<T>
    fun launch(lambda: () -> Unit)
}

class NavStackEntryResultBinder<T : Parcelable>(
    navController: NavController,
    selfRoute: String,
    key: String,
) : ResultBinder<T> {
    private val state = ((navController.currentBackStackEntry ?: throw IllegalStateException())
        .takeIf { it.destination.route == selfRoute }
        ?: (navController.previousBackStackEntry ?: throw IllegalStateException()))
        .also { if (BuildConfig.DEBUG && it.destination.route != selfRoute) { throw IllegalStateException() } }
        .savedStateHandle

    @Suppress("UNCHECKED_CAST")
    override val results: Flow<T> = state
        .getStateFlow<Any>(key = key, SENTINEL)
        .filter { it != SENTINEL }
        .onEach { state[key] = SENTINEL }
        as Flow<T>

    override fun launch(lambda: () -> Unit) {
        lambda()
    }
}

inline fun <reified T : Parcelable> NavController.bindResult(selfRoute: String): ResultBinder<T> =
    NavStackEntryResultBinder(
        navController = this,
        selfRoute = selfRoute,
        key = T::class.qualifiedName!!,
    )

inline fun <reified T : Parcelable> NavController.returnResult(result: T) {
    val entry = previousBackStackEntry ?: return
    entry.savedStateHandle[T::class.qualifiedName!!] = result
}

@Parcelize
private data object SENTINEL : Parcelable
