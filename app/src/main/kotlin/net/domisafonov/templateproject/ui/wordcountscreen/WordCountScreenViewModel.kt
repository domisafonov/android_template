package net.domisafonov.templateproject.ui.wordcountscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import net.domisafonov.templateproject.domain.usecase.ObserveWordCountTextUc
import net.domisafonov.templateproject.domain.usecase.RefreshAboutPageUc
import javax.inject.Inject

@HiltViewModel
class WordCountScreenViewModel @Inject constructor(
    observeWordCountTextUc: ObserveWordCountTextUc,
    private val refreshAboutPageUc: RefreshAboutPageUc,
) : ViewModel() {

    val wordCountText: Flow<String?> = observeWordCountTextUc.execute()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val _isRefreshCompleted = MutableStateFlow(false)
    val isRefreshCompleted: StateFlow<Boolean> = _isRefreshCompleted.asStateFlow()

    fun setRefreshing(isRefreshing: Boolean) {
        if (isRefreshing) {
            viewModelScope.launch {
                refreshAboutPageUc.execute()
                _isRefreshCompleted.value = true
            }
        } else {
            _isRefreshCompleted.value = false
        }
    }
}
