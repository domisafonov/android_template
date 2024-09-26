package net.domisafonov.templateproject.data.settings

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.data.settings_store.proto.ProtoSettings
import net.domisafonov.templateproject.data.settings_store.proto.protoSettings
import net.domisafonov.templateproject.domain.model.Settings
import net.domisafonov.templateproject.domain.repository.SettingsRepository
import net.domisafonov.templateproject.ui.DEFAULT_PAGE_URL

class ProtoSettingsRepository(
    private val dataStore: DataStore<ProtoSettings>,
) : SettingsRepository {

    override fun observeSettings(): Flow<Settings> = dataStore.data
        .map {
            Settings(
                url = it.url.takeUnless { it.isBlank() } ?: DEFAULT_PAGE_URL,
            )
        }

    override suspend fun setSettings(new: Settings) {
        dataStore.updateData {
            protoSettings {
                url = new.url
            }
        }
    }
}
