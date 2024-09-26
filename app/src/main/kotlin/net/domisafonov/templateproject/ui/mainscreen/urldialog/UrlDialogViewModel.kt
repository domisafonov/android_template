package net.domisafonov.templateproject.ui.mainscreen.urldialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.domisafonov.templateproject.R
import net.domisafonov.templateproject.domain.usecase.GetCurrentUrlUc
import net.domisafonov.templateproject.domain.usecase.SaveUrlUc
import net.domisafonov.templateproject.domain.usecase.ValidateUrlInputUc
import net.domisafonov.templateproject.ui.util.ResourceDelegate
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UrlDialogViewModel @Inject constructor(
    private val getCurrentUrlUc: GetCurrentUrlUc,
    private val validateUrlInputUc: ValidateUrlInputUc,
    private val saveUrlUc: SaveUrlUc,
    private val resources: ResourceDelegate,
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isDismissed = MutableStateFlow(false)
    val isDismissed: StateFlow<Boolean> = _isDismissed.asStateFlow()

    init {
        viewModelScope.launch {
            _text.value = getCurrentUrlUc.execute()
        }
    }

    fun onTextChanged(value: String) {
        _text.value = value

        viewModelScope.launch {
            if (validateUrlInputUc.execute(value)) {
                _error.value = null
            } else {
                _error.value = resources.getString(R.string.invalid_url_error)
            }
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            if (_error.value != null) {
                Timber.wtf("trying to save erroroneous URL")
                return@launch
            }
            saveUrlUc.execute(_text.value)
            _isDismissed.value = true
        }
    }
}
