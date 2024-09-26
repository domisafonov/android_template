package net.domisafonov.templateproject.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.domain.repository.PageRepository
import net.domisafonov.templateproject.ui.ABOUT_PAGE_URL
import javax.inject.Inject

fun interface ObserveTenthCharacterTextUc {
    fun execute(): Flow<String?>
}

class ObserveTenthCharacterTextUcImpl @Inject constructor(
    private val pageRepository: PageRepository,
    private val makeTenthCharacterTextUc: MakeTenthCharacterTextUc,
) : ObserveTenthCharacterTextUc {

    override fun execute(): Flow<String?> = pageRepository.observePage(ABOUT_PAGE_URL)
        .map { page -> page?.let { makeTenthCharacterTextUc.execute(page.contents) } }
}
