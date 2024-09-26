package net.domisafonov.templateproject.ui

import android.net.ConnectivityManager
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import net.domisafonov.templateproject.data.android.isOffline

@Stable
class AppState(
    val navController: NavHostController,
    val appBarActions: MutableState<(@Composable RowScope.() -> Unit)?>,
    val isOffline: State<Boolean?>,
    val snackbarHostState: SnackbarHostState,
)

@Composable
fun ComposeApp(
    modifier: Modifier = Modifier,
) {
    val isOffline = getIsOffline()
    val navController = rememberNavController()
    val appState = remember {
        AppState(
            navController = navController,
            appBarActions = mutableStateOf(null),
            isOffline = isOffline,
            snackbarHostState = SnackbarHostState(),
        )
    }

    UiRoot(
        appState = appState,
        modifier = modifier,
    )
}

@Composable
private fun getIsOffline(): State<Boolean?> {
    val context = LocalContext.current
    val cm = remember(context) { context.getSystemService(ConnectivityManager::class.java) }
    return cm.isOffline().collectAsState(initial = null)
}
