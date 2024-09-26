package net.domisafonov.templateproject.ui.mainscreen.urldialog

import androidx.navigation.NavController

interface UrlDialogCoordinator {
    fun goBack()
}

class UrlDialogCoordinatorImpl(
    private val navHostController: NavController,
) : UrlDialogCoordinator {
    override fun goBack() {
        navHostController.popBackStack()
    }
}
