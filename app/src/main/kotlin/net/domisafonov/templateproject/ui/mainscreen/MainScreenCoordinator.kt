package net.domisafonov.templateproject.ui.mainscreen

import androidx.navigation.NavController

interface MainScreenCoordinator {
    fun openTenthCharacter()
    fun openWordCount()
    fun openUrlEditor()
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

    override fun openUrlEditor() {
        navController.navigate("main/url_dialog")
    }
}
