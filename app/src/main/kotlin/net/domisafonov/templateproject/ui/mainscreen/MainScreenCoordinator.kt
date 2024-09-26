package net.domisafonov.templateproject.ui.mainscreen

import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import net.domisafonov.templateproject.ui.navigation.Coordinator
import net.domisafonov.templateproject.ui.mainscreen.urldialog.UrlDialogResult
import net.domisafonov.templateproject.ui.navigation.bindResult

interface MainScreenCoordinator : Coordinator {
    fun openTenthCharacter()

    fun openWordCount()

    fun openUrlEditor()
    val urlEditorResult: Flow<UrlDialogResult>
}

class MainScreenCoordinatorImpl(
    private val navController: NavController,
) : MainScreenCoordinator {
    override fun openTenthCharacter() {
        navController.navigate("details/tenth_character")
    }

    override fun openWordCount() {
        navController.navigate("details/word_count")
    }

    private val urlEditorBinder = navController.bindResult<UrlDialogResult>(selfRoute = "main")
    override fun openUrlEditor() {
        urlEditorBinder.launch {
            navController.navigate("main/url_dialog")
        }
    }
    override val urlEditorResult: Flow<UrlDialogResult> get() = urlEditorBinder.results
}
