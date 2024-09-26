package net.domisafonov.templateproject.di.module

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.domisafonov.templateproject.di.IoDispatcher
import net.domisafonov.templateproject.domain.repository.PageRepository
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
        makeWordCountTextUc: MakeWordCountTextUc,
    ): ObserveWordCountTextUc = ObserveWordCountTextUcImpl(
        pageRepository = pageRepository,
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
        makeTenthCharacterTextUc: MakeTenthCharacterTextUc,
    ): ObserveTenthCharacterTextUc = ObserveTenthCharacterTextUcImpl(
        pageRepository = pageRepository,
        makeTenthCharacterTextUc = makeTenthCharacterTextUc,
    )

    @Provides
    @Reusable
    fun refreshAboutPage(
        pageRepository: PageRepository,
    ): RefreshAboutPageUc = RefreshAboutPageUcImpl(
        pageRepository = pageRepository,
    )
}
