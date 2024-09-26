package net.domisafonov.templateproject.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.dialog
import androidx.navigation.get
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.consumeAsFlow
import net.domisafonov.templateproject.R
import net.domisafonov.templateproject.ui.tenthcharacterscreen.TenthCharacterScreen
import net.domisafonov.templateproject.ui.mainscreen.MainScreen
import net.domisafonov.templateproject.ui.mainscreen.MainScreenAppBar
import net.domisafonov.templateproject.ui.mainscreen.MainScreenAppBarEvent
import net.domisafonov.templateproject.ui.mainscreen.MainScreenAppBarState
import net.domisafonov.templateproject.ui.mainscreen.urldialog.UrlDialog
import net.domisafonov.templateproject.ui.topbar.AppBarController
import net.domisafonov.templateproject.ui.wordcountscreen.WordCountScreen
import timber.log.Timber

const val MAIN_NAV_ID = "main"

@Composable
fun NavHost(
    appState: AppState,
    modifier: Modifier = Modifier,
) {
    val mainLabel = stringResource(id = R.string.main_screen_label)
    val tenthLabel = stringResource(id = R.string.tenth_screen_label)
    val wordCountLabel = stringResource(id = R.string.word_count_screen_label)

    NavHost(
        navController = appState.navController,
        startDestination = MAIN_NAV_ID,
        modifier = modifier,
    ) {
        composable<MainScreenAppBarState, MainScreenAppBarEvent>(
            appState = appState,
            route = MAIN_NAV_ID,
            appBar = { state, events -> MainScreenAppBar(state, events) },
            label = mainLabel,
        ) { _, controller ->
            MainScreen(
                onTenthClick = { appState.navController.navigate("details/tenth_character") },
                onWordCountClick = { appState.navController.navigate("details/word_count") },
                onUrlButtonClick = { appState.navController.navigate("main/url_dialog") },
                appBarController = controller,
            )
        }
        dialog(route = "main/url_dialog") { UrlDialog(onDismiss = { appState.navController.popBackStack() }) }

        composable(appState = appState, route = "details/tenth_character", label = tenthLabel) { TenthCharacterScreen() }
        composable(appState = appState, route = "details/word_count", label = wordCountLabel) { WordCountScreen() }
    }
}

private fun <AppBarState : Any, AppBarEvent : Any> NavGraphBuilder.composable(
    appState: AppState,
    route: String,
    appBar: @Composable RowScope.(state: State<AppBarState?>, events: SendChannel<AppBarEvent>) -> Unit,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    label: String? = null,
    content: @Composable (entry: NavBackStackEntry, appBarController: AppBarController<AppBarState, AppBarEvent>) -> Unit,
) {
    addDestination(
        ComposeNavigator.Destination(provider[ComposeNavigator::class]) { entry ->
            val stateChannel = Channel<AppBarState>(Channel.CONFLATED)
            val eventChannel = Channel<AppBarEvent>(
                capacity = EVENT_CAPACITY,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
                onUndeliveredElement = { Timber.e("undelivered app bar event: $it") },
            )

            appState.appBarActions.value = {
                appBar(
                    this,
                    stateChannel.consumeAsFlow().collectAsState(initial = null),
                    eventChannel,
                )
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

private fun NavGraphBuilder.composable(
    appState: AppState,
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
