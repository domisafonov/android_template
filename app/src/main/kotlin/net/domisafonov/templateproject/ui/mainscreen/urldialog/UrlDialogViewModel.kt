package net.domisafonov.templateproject.ui.mainscreen.urldialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UrlDialogViewModel @Inject constructor(

) : ViewModel() {

    private val _text = MutableStateFlow("") // TODO: init
    val text: StateFlow<String> = _text.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isDismissed = MutableStateFlow(false)
    val isDismissed: StateFlow<Boolean> = _isDismissed.asStateFlow()

    fun onTextChanged(value: String) {
        // TODO: error
        _text.value = value
    }

    fun onSaveClick() {
        // TODO: saving, error
        _isDismissed.value = true
    }
}
