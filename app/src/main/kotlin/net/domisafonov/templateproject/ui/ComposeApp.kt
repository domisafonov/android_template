package net.domisafonov.templateproject.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import net.domisafonov.templateproject.ui.theme.TemplateProjectTheme
import net.domisafonov.templateproject.ui.topbar.TopBar

@Stable
class AppState(
    val navController: NavHostController,
    val appBarActions: MutableState<(@Composable RowScope.() -> Unit)?>,
)

@Composable
fun ComposeApp(
    appState: AppState = AppState(
        navController = rememberNavController(),
        appBarActions = mutableStateOf(null),
    ),
    modifier: Modifier = Modifier,
) {
    val appState = remember { appState }

    TemplateProjectTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = { TopBar(appState) },
        ) { innerPadding ->
            NavHost(
                appState = appState,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
