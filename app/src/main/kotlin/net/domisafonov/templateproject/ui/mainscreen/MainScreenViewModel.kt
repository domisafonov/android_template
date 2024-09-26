package net.domisafonov.templateproject.ui.mainscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import net.domisafonov.templateproject.mvi.sendWish
import javax.inject.Inject

// TODO: remember to implement saving state (for process death)
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    actor: MainScreenMvi.Actor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mvi = MainScreenMvi.Component(
        scope = viewModelScope,
        actor = actor,
    )

    val state: StateFlow<MainScreenMvi.State> = mvi.state // TODO: to viewstate
    val commands: Flow<MainScreenMvi.Command> = mvi.sideEffects.filterIsInstance()
    val navigation: Flow<MainScreenMvi.Navigation> = mvi.sideEffects.filterIsInstance()

    fun onGoButtonClick() {
        mvi.sendWish(MainScreenMvi.Wish.GoButtonClick)
    }

    fun onTenthClick() {
        mvi.sendWish(MainScreenMvi.Wish.TenthClick)
    }

    fun onWordCountClick() {
        mvi.sendWish(MainScreenMvi.Wish.WordCountClick)
    }

    fun onUrlButtonClick() {
        mvi.sendWish(MainScreenMvi.Wish.UrlButtonClick)
    }
}
