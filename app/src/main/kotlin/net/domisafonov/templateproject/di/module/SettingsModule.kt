package net.domisafonov.templateproject.di.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import net.domisafonov.templateproject.data.settings.PROTO_SETTINGS_FILENAME
import net.domisafonov.templateproject.data.settings.ProtoSettingsRepository
import net.domisafonov.templateproject.data.settings.ProtoSettingsSerializer
import net.domisafonov.templateproject.data.settings_store.proto.ProtoSettings
import net.domisafonov.templateproject.di.BackgroundIoScope
import net.domisafonov.templateproject.domain.repository.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    fun settingsDataStore(
        @ApplicationContext context: Context,
        @BackgroundIoScope backgroundIoScope: CoroutineScope,
    ): DataStore<ProtoSettings> = DataStoreHelper(
        context = context,
        backgroundIoScope = backgroundIoScope,
    ).stored

    @Provides
    @Singleton
    fun settingsRepository(
        dataStore: DataStore<ProtoSettings>,
    ): SettingsRepository = ProtoSettingsRepository(
        dataStore = dataStore,
    )

    private class DataStoreHelper(
        context: Context,
        backgroundIoScope: CoroutineScope,
    ) {
        private val Context.store by dataStore(
            fileName = PROTO_SETTINGS_FILENAME,
            serializer = ProtoSettingsSerializer,
            scope = backgroundIoScope,
        )

        val stored: DataStore<ProtoSettings> = context.store
    }
}
