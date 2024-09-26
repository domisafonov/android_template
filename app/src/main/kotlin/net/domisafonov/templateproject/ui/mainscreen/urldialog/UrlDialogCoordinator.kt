package net.domisafonov.templateproject.ui.mainscreen.urldialog

import androidx.navigation.NavController
import net.domisafonov.templateproject.ui.navigation.Coordinator
import net.domisafonov.templateproject.ui.navigation.returnResult

interface UrlDialogCoordinator : Coordinator {
    fun goBack(isSuccessful: Boolean = false)
}

class UrlDialogCoordinatorImpl(
    private val navHostController: NavController,
) : UrlDialogCoordinator {
    override fun goBack(isSuccessful: Boolean) {
        if (isSuccessful) {
            navHostController.returnResult(UrlDialogResult())
        }
        navHostController.popBackStack()
    }
}
