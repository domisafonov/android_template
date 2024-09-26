package net.domisafonov.templateproject.domain.usecase

import net.domisafonov.templateproject.domain.repository.PageRepository
import net.domisafonov.templateproject.ui.DEFAULT_PAGE_URL

fun interface RefreshAboutPageUc {
    suspend fun execute()
}

class RefreshAboutPageUcImpl(
    private val pageRepository: PageRepository,
) : RefreshAboutPageUc {
    override suspend fun execute() {
        pageRepository.forceUpdatePage(DEFAULT_PAGE_URL)
    }
}
