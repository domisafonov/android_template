package net.domisafonov.templateproject.domain.repository

import kotlinx.coroutines.flow.Flow
import net.domisafonov.templateproject.domain.model.Settings

interface SettingsRepository {
    fun observeSettings(): Flow<Settings>
    suspend fun setSettings(new: Settings)
}
