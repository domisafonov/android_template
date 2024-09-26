package net.domisafonov.templateproject.domain.usecase

import net.domisafonov.templateproject.domain.repository.SettingsRepository
import net.domisafonov.templateproject.domain.repository.updateSettings

fun interface SaveUrlUc {
    suspend fun execute(newUrl: String)
}

class SaveUrlUcImpl(
    private val settingsRepository: SettingsRepository,
    private val validateUrlInputUc: ValidateUrlInputUc,
) : SaveUrlUc {
    override suspend fun execute(newUrl: String) {
        if (!validateUrlInputUc.execute(newUrl)) {
            throw IllegalArgumentException()
        }
        settingsRepository.updateSettings { it.copy(url = newUrl) }
    }
}
