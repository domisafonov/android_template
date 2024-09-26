package net.domisafonov.templateproject.ui.mainscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val IS_ACTIVATED_KEY = "isActivated"
    }

    private val _isActivated = MutableStateFlow(savedStateHandle[IS_ACTIVATED_KEY] ?: false)
    val isActivated = _isActivated.asStateFlow()

    init {
        viewModelScope.launch {
            isActivated.collect {
                savedStateHandle[IS_ACTIVATED_KEY] = it
            }
        }
    }

    fun onButtonClick() {
        _isActivated.value = true
    }
}
