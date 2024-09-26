package net.domisafonov.templateproject.domain.usecase

import kotlinx.coroutines.flow.first
import net.domisafonov.templateproject.domain.repository.SettingsRepository
import net.domisafonov.templateproject.ui.DEFAULT_PAGE_URL
import javax.inject.Inject

fun interface GetCurrentUrlUc {
    suspend fun execute(): String
}

class GetCurrentUrlUcImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : GetCurrentUrlUc {
    override suspend fun execute(): String = settingsRepository.observeSettings().first()
        .url
        .ifBlank { DEFAULT_PAGE_URL }
}
