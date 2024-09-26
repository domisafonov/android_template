package net.domisafonov.templateproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

interface Coordinator

@Composable
fun <C : Coordinator> rememberCoordinator(builder: () -> C): C {
    return remember { builder() }
}
