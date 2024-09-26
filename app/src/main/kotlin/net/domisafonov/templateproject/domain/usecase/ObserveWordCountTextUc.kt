package net.domisafonov.templateproject.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.domain.repository.PageRepository
import net.domisafonov.templateproject.ui.ABOUT_PAGE_URL

fun interface ObserveWordCountTextUc {
    fun execute(): Flow<List<String>?>
}

class ObserveWordCountTextUcImpl(
    private val pageRepository: PageRepository,
    private val makeWordCountTextUc: MakeWordCountTextUc,
) : ObserveWordCountTextUc {

    override fun execute(): Flow<List<String>?> = pageRepository.observePage(ABOUT_PAGE_URL)
        .map { page -> page?.let { makeWordCountTextUc.execute(page.contents) } }
}
