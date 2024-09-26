package net.domisafonov.templateproject.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.domain.repository.PageRepository
import net.domisafonov.templateproject.domain.repository.SettingsRepository
import javax.inject.Inject

fun interface ObserveTenthCharacterTextUc {
    fun execute(): Flow<String?>
}

class ObserveTenthCharacterTextUcImpl @Inject constructor(
    private val pageRepository: PageRepository,
    private val settingsRepository: SettingsRepository,
    private val makeTenthCharacterTextUc: MakeTenthCharacterTextUc,
) : ObserveTenthCharacterTextUc {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(): Flow<String?> = settingsRepository
        .observeSettings()
        .flatMapLatest { pageRepository.observePage(it.url) }
        .map { page -> page?.let { makeTenthCharacterTextUc.execute(page.contents) } }
}
