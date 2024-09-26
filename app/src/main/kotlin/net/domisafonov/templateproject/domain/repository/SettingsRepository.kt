package net.domisafonov.templateproject.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import net.domisafonov.templateproject.domain.model.Settings

interface SettingsRepository {
    fun observeSettings(): Flow<Settings>
    suspend fun setSettings(new: Settings)
}

suspend fun SettingsRepository.updateSettings(updater: (current: Settings) -> Settings) {
    val current = observeSettings().first()
    val new = updater(current)
    if (new != current) {
        setSettings(new)
    }
}
