package net.domisafonov.templateproject.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.get
import net.domisafonov.templateproject.R
import net.domisafonov.templateproject.ui.tenthcharacterscreen.TenthCharacterScreen
import net.domisafonov.templateproject.ui.mainscreen.MainScreen
import net.domisafonov.templateproject.ui.wordcountscreen.WordCountScreen

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
        composable(route = MAIN_NAV_ID, label = mainLabel) {
            MainScreen(
                onTenthClick = { appState.navController.navigate("details/tenchcharacter") },
                onWordCountClick = { appState.navController.navigate("details/wordcount") },
            )
        }

        composable(route = "details/tenchcharacter", label = tenthLabel) { TenthCharacterScreen() }
        composable(route = "details/wordcount", label = wordCountLabel) { WordCountScreen() }
    }
}

private fun NavGraphBuilder.composable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    label: String? = null,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    addDestination(
        ComposeNavigator.Destination(provider[ComposeNavigator::class], content).apply {
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
