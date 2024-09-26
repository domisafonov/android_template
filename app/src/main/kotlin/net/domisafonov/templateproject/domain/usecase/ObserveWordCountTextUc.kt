package net.domisafonov.templateproject.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.domain.repository.PageRepository
import net.domisafonov.templateproject.domain.repository.SettingsRepository

fun interface ObserveWordCountTextUc {
    fun execute(): Flow<List<String>?>
}

class ObserveWordCountTextUcImpl(
    private val pageRepository: PageRepository,
    private val settingsRepository: SettingsRepository,
    private val makeWordCountTextUc: MakeWordCountTextUc,
) : ObserveWordCountTextUc {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(): Flow<List<String>?> = settingsRepository
        .observeSettings()
        .flatMapLatest { pageRepository.observePage(it.url) }
        .map { page -> page?.let { makeWordCountTextUc.execute(page.contents) } }
}
