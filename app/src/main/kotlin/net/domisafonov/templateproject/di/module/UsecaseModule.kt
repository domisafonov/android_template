package net.domisafonov.templateproject.di.module

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.domisafonov.templateproject.di.IoDispatcher
import net.domisafonov.templateproject.domain.repository.PageRepository
import net.domisafonov.templateproject.domain.repository.SettingsRepository
import net.domisafonov.templateproject.domain.usecase.GetCurrentUrlUc
import net.domisafonov.templateproject.domain.usecase.GetCurrentUrlUcImpl
import net.domisafonov.templateproject.domain.usecase.MakeTenthCharacterTextUc
import net.domisafonov.templateproject.domain.usecase.MakeTenthCharacterTextUcImpl
import net.domisafonov.templateproject.domain.usecase.MakeWordCountTextUc
import net.domisafonov.templateproject.domain.usecase.MakeWordCountTextUcImpl
import net.domisafonov.templateproject.domain.usecase.ObserveTenthCharacterTextUc
import net.domisafonov.templateproject.domain.usecase.ObserveTenthCharacterTextUcImpl
import net.domisafonov.templateproject.domain.usecase.ObserveWordCountTextUc
import net.domisafonov.templateproject.domain.usecase.ObserveWordCountTextUcImpl
import net.domisafonov.templateproject.domain.usecase.RefreshAboutPageUc
import net.domisafonov.templateproject.domain.usecase.RefreshAboutPageUcImpl
import net.domisafonov.templateproject.domain.usecase.SaveUrlUc
import net.domisafonov.templateproject.domain.usecase.SaveUrlUcImpl
import net.domisafonov.templateproject.domain.usecase.ValidateUrlInputUc
import net.domisafonov.templateproject.domain.usecase.ValidateUrlInputUcImpl

@Module
@InstallIn(SingletonComponent::class)
object UsecaseModule {

    @Provides
    @Reusable
    fun makeWordCountText(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): MakeWordCountTextUc = MakeWordCountTextUcImpl(
        ioDispatcher = ioDispatcher,
    )

    @Provides
    @Reusable
    fun observeWordCountText(
        pageRepository: PageRepository,
        settingsRepository: SettingsRepository,
        makeWordCountTextUc: MakeWordCountTextUc,
    ): ObserveWordCountTextUc = ObserveWordCountTextUcImpl(
        pageRepository = pageRepository,
        settingsRepository = settingsRepository,
        makeWordCountTextUc = makeWordCountTextUc,
    )

    @Provides
    @Reusable
    fun makeTenthCharacterText(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): MakeTenthCharacterTextUc = MakeTenthCharacterTextUcImpl(
        ioDispatcher = ioDispatcher,
    )

    @Provides
    @Reusable
    fun observeTenthCharacterText(
        pageRepository: PageRepository,
        settingsRepository: SettingsRepository,
        makeTenthCharacterTextUc: MakeTenthCharacterTextUc,
    ): ObserveTenthCharacterTextUc = ObserveTenthCharacterTextUcImpl(
        pageRepository = pageRepository,
        settingsRepository = settingsRepository,
        makeTenthCharacterTextUc = makeTenthCharacterTextUc,
    )

    @Provides
    @Reusable
    fun refreshAboutPage(
        pageRepository: PageRepository,
    ): RefreshAboutPageUc = RefreshAboutPageUcImpl(
        pageRepository = pageRepository,
    )

    @Provides
    @Reusable
    fun validateUrlInput(): ValidateUrlInputUc = ValidateUrlInputUcImpl()

    @Provides
    @Reusable
    fun getCurrentUrl(
        settingsRepository: SettingsRepository,
    ): GetCurrentUrlUc = GetCurrentUrlUcImpl(
        settingsRepository = settingsRepository,
    )

    @Provides
    @Reusable
    fun saveUrl(
        settingsRepository: SettingsRepository,
        validateUrlInputUc: ValidateUrlInputUc,
    ): SaveUrlUc = SaveUrlUcImpl(
        settingsRepository = settingsRepository,
        validateUrlInputUc = validateUrlInputUc,
    )
}
