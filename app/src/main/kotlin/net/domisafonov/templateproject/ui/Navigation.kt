package net.domisafonov.templateproject.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.get
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.shareIn
import net.domisafonov.templateproject.R
import net.domisafonov.templateproject.ui.tenthcharacterscreen.TenthCharacterScreen
import net.domisafonov.templateproject.ui.mainscreen.MainScreen
import net.domisafonov.templateproject.ui.mainscreen.MainScreenAppBar
import net.domisafonov.templateproject.ui.mainscreen.MainScreenAppBarState
import net.domisafonov.templateproject.ui.topbar.AppBarController
import net.domisafonov.templateproject.ui.wordcountscreen.WordCountScreen
import timber.log.Timber

const val MAIN_NAV_ID = "main"

@Composable
fun NavHost(
    appState: AppState,
    modifier: Modifier = Modifier,
) {
    fun <AppBarState : Any, AppBarEvent : Any> NavGraphBuilder.composable(
        route: String,
        appBar: @Composable RowScope.(state: Flow<AppBarState>, events: SendChannel<AppBarEvent>) -> Unit,
        arguments: List<NamedNavArgument> = emptyList(),
        deepLinks: List<NavDeepLink> = emptyList(),
        label: String? = null,
        content: @Composable (entry: NavBackStackEntry, appBarController: AppBarController<AppBarState, AppBarEvent>) -> Unit,
    ) {
        addDestination(
            ComposeNavigator.Destination(provider[ComposeNavigator::class]) { entry ->

                val coroutineScope = rememberCoroutineScope()
                val stateChannel = Channel<AppBarState>(Channel.CONFLATED)
                val eventChannel = Channel<AppBarEvent>(
                    capacity = EVENT_CAPACITY,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                    onUndeliveredElement = { Timber.e("undelivered app bar event: $it") },
                )

                LaunchedEffect(Unit) {
                    appState.appBarActions.value = {
                        appBar(
                            this,
                            stateChannel.consumeAsFlow().shareIn(
                                scope = coroutineScope,
                                started = SharingStarted.Lazily,
                                replay = 1,
                            ),
                            eventChannel,
                        )
                    }
                }

                content(
                    entry,
                    AppBarController(
                        state = stateChannel,
                        events = eventChannel.consumeAsFlow(),
                    ),
                )
            }.apply {
                this.route = route
                arguments.forEach { (argumentName, argument) ->
                    addArgument(argumentName, argument)
                }
                deepLinks.forEach { deepLink ->
                    addDeepLink(deepLink)
                }
                this.label = label
            }
        )
    }

    fun NavGraphBuilder.composable(
        route: String,
        arguments: List<NamedNavArgument> = emptyList(),
        deepLinks: List<NavDeepLink> = emptyList(),
        label: String? = null,
        content: @Composable (entry: NavBackStackEntry) -> Unit,
    ) {
        addDestination(
            ComposeNavigator.Destination(provider[ComposeNavigator::class]) { entry ->
                appState.appBarActions.value = null
                content(entry)
            }.apply {
                this.route = route
                arguments.forEach { (argumentName, argument) ->
                    addArgument(argumentName, argument)
                }
                deepLinks.forEach { deepLink ->
                    addDeepLink(deepLink)
                }
                this.label = label
            }
        )
    }

    val mainLabel = stringResource(id = R.string.main_screen_label)
    val tenthLabel = stringResource(id = R.string.tenth_screen_label)
    val wordCountLabel = stringResource(id = R.string.word_count_screen_label)

    NavHost(
        navController = appState.navController,
        startDestination = MAIN_NAV_ID,
        modifier = modifier,
    ) {
        composable<MainScreenAppBarState, Unit>(
            route = MAIN_NAV_ID,
            appBar = { state, events -> MainScreenAppBar(state, events) },
            label = mainLabel,
        ) { _, controller ->
            MainScreen(
                onTenthClick = { appState.navController.navigate("details/tenchcharacter") },
                onWordCountClick = { appState.navController.navigate("details/wordcount") },
                appBarController = controller,
            )
        }

        composable(route = "details/tenchcharacter", label = tenthLabel) { TenthCharacterScreen() }
        composable(route = "details/wordcount", label = wordCountLabel) { WordCountScreen() }
    }
}
